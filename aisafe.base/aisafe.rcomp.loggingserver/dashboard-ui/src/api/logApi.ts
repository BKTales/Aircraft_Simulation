export async function fetchActiveUsers(): Promise<string[]> {
  const res = await fetch("/api/active-users");
  if (!res.ok) throw new Error("API request failed");
  return res.json();
}

export async function fetchEvents(): Promise<string[]> {
  const res = await fetch("/api/events");
  if (!res.ok) throw new Error("API request failed");
  return res.json();
}
