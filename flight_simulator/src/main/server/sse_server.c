#include "simulator_includes.h"
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <arpa/inet.h>
#include <fcntl.h>
#include <pthread.h>
#include <poll.h>

#define MAX_CLIENTS 16

static pthread_t g_accept_thread;
static volatile sig_atomic_t g_running = 0;
static int g_thread_started = 0;

static int g_listen_fd = -1;
static int g_clients[MAX_CLIENTS];
static pthread_mutex_t g_clients_mux = PTHREAD_MUTEX_INITIALIZER;
static char g_viewer_path[PATH_MAX];

static const char *SSE_HEADERS =
    "HTTP/1.1 200 OK\r\n"
    "Content-Type: text/event-stream\r\n"
    "Cache-Control: no-cache\r\n"
    "Connection: keep-alive\r\n"
    "Access-Control-Allow-Origin: *\r\n"
    "\r\n";

static void add_client(int fd) {
    pthread_mutex_lock(&g_clients_mux);
    for (int i = 0; i < MAX_CLIENTS; i++) {
        if (g_clients[i] == -1) {
            g_clients[i] = fd;
            break;
        }
    }
    pthread_mutex_unlock(&g_clients_mux);
}

static int parse_request_path(const char *buf, char *path_out, size_t path_size) {
    char method[16];
    if (sscanf(buf, "%15s %255s", method, path_out) < 2)
        return -1;
    (void)method;
    if (path_size > 0)
        path_out[path_size - 1] = '\0';
    return 0;
}

static int is_viewer_request(const char *path) {
    return strcmp(path, "/") == 0
        || strcmp(path, "/viewer.html") == 0
        || strcmp(path, "/index.html") == 0;
}

static int is_stream_request(const char *path) {
    return strcmp(path, "/stream") == 0;
}

static void send_http_response(int fd, int status_code, const char *status_text,
                               const char *content_type, const char *body, size_t body_len) {
    char header[512];
    int n = snprintf(header, sizeof(header),
        "HTTP/1.1 %d %s\r\n"
        "Content-Type: %s\r\n"
        "Content-Length: %zu\r\n"
        "Connection: close\r\n"
        "Access-Control-Allow-Origin: *\r\n"
        "\r\n",
        status_code, status_text, content_type, body_len);
    if (n > 0)
        send(fd, header, (size_t)n, MSG_NOSIGNAL);
    if (body && body_len > 0)
        send(fd, body, body_len, MSG_NOSIGNAL);
}

static int serve_viewer_html(int fd) {
    if (!g_viewer_path[0])
        return -1;

    FILE *f = fopen(g_viewer_path, "rb");
    if (!f)
        return -1;

    if (fseek(f, 0, SEEK_END) != 0) {
        fclose(f);
        return -1;
    }
    long size = ftell(f);
    if (size < 0) {
        fclose(f);
        return -1;
    }
    rewind(f);

    char *body = malloc((size_t)size + 1);
    if (!body) {
        fclose(f);
        return -1;
    }

    size_t read_n = fread(body, 1, (size_t)size, f);
    fclose(f);
    if (read_n != (size_t)size) {
        free(body);
        return -1;
    }
    body[size] = '\0';

    send_http_response(fd, 200, "OK", "text/html; charset=utf-8", body, (size_t)size);
    free(body);
    return 0;
}

static void *accept_loop(void *arg) {
    (void)arg;
    struct pollfd pfd;
    pfd.fd = g_listen_fd;
    pfd.events = POLLIN;

    while (g_running) {
        int pr = poll(&pfd, 1, 200);
        if (pr <= 0) continue;
        if (!(pfd.revents & POLLIN)) continue;

        struct sockaddr_in addr;
        socklen_t alen = sizeof(addr);
        int fd = accept(g_listen_fd, (struct sockaddr *)&addr, &alen);
        if (fd < 0) continue;

        char buf[1024];
        ssize_t nread = recv(fd, buf, sizeof(buf) - 1, 0);
        if (nread <= 0) {
            close(fd);
            continue;
        }
        buf[nread] = '\0';

        char path[256] = {0};
        if (parse_request_path(buf, path, sizeof(path)) != 0) {
            close(fd);
            continue;
        }

        if (is_viewer_request(path)) {
            if (serve_viewer_html(fd) != 0) {
                const char *msg = "Viewer HTML not found.";
                send_http_response(fd, 404, "Not Found", "text/plain; charset=utf-8",
                                   msg, strlen(msg));
            }
            close(fd);
            continue;
        }

        if (!is_stream_request(path)) {
            const char *msg = "Use / for the viewer or /stream for SSE.";
            send_http_response(fd, 404, "Not Found", "text/plain; charset=utf-8",
                               msg, strlen(msg));
            close(fd);
            continue;
        }

        if (send(fd, SSE_HEADERS, strlen(SSE_HEADERS), MSG_NOSIGNAL) < 0) {
            close(fd);
            continue;
        }

        int one = 1;
        setsockopt(fd, IPPROTO_TCP, TCP_NODELAY, &one, sizeof(one));
        fcntl(fd, F_SETFL, O_NONBLOCK);
        add_client(fd);
    }
    return NULL;
}

int sse_server_start(int port, const char *viewer_html_path) {
    for (int i = 0; i < MAX_CLIENTS; i++) g_clients[i] = -1;
    g_viewer_path[0] = '\0';
    if (viewer_html_path && viewer_html_path[0]) {
        strncpy(g_viewer_path, viewer_html_path, sizeof(g_viewer_path) - 1);
        g_viewer_path[sizeof(g_viewer_path) - 1] = '\0';
    }

    signal(SIGPIPE, SIG_IGN);

    g_listen_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (g_listen_fd < 0) return -1;

    int opt = 1;
    setsockopt(g_listen_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

    struct sockaddr_in addr = {0};
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = INADDR_ANY;
    addr.sin_port = htons((uint16_t)port);

    if (bind(g_listen_fd, (struct sockaddr *)&addr, sizeof(addr)) != 0) {
        close(g_listen_fd);
        g_listen_fd = -1;
        return -1;
    }
    if (listen(g_listen_fd, 8) != 0) {
        close(g_listen_fd);
        g_listen_fd = -1;
        return -1;
    }

    g_running = 1;
    if (pthread_create(&g_accept_thread, NULL, accept_loop, NULL) != 0) {
        g_running = 0;
        close(g_listen_fd);
        g_listen_fd = -1;
        g_thread_started = 0;
        return -1;
    }
    g_thread_started = 1;
    return 0;
}

void sse_server_broadcast(const char *json_line) {
    char frame[1200];
    int n = snprintf(frame, sizeof(frame), "data: %s\n\n", json_line);
    if (n <= 0) return;

    pthread_mutex_lock(&g_clients_mux);
    for (int i = 0; i < MAX_CLIENTS; i++) {
        if (g_clients[i] == -1) continue;
        ssize_t sent = send(g_clients[i], frame, (size_t)n, MSG_NOSIGNAL);
        if (sent < 0 && errno != EWOULDBLOCK && errno != EAGAIN) {
            close(g_clients[i]);
            g_clients[i] = -1;
        }
    }
    pthread_mutex_unlock(&g_clients_mux);
}

void sse_server_stop(void) {
    g_running = 0;

    if (g_listen_fd >= 0) {
        shutdown(g_listen_fd, SHUT_RDWR);
        close(g_listen_fd);
        g_listen_fd = -1;
    }

    pthread_mutex_lock(&g_clients_mux);
    for (int i = 0; i < MAX_CLIENTS; i++) {
        if (g_clients[i] != -1) {
            shutdown(g_clients[i], SHUT_RDWR);
            close(g_clients[i]);
            g_clients[i] = -1;
        }
    }
    pthread_mutex_unlock(&g_clients_mux);

    if (g_thread_started) {
        pthread_join(g_accept_thread, NULL);
        g_thread_started = 0;
    }


}