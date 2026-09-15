#ifndef SSE_SERVER_H
#define SSE_SERVER_H

int  sse_server_start(int port, const char *viewer_html_path);
void sse_server_broadcast(const char *json_line);
void sse_server_stop(void);

#endif