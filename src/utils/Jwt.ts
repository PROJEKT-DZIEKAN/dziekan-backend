export function getUserIdFromToken(): string | null {
  const token = localStorage.getItem('token')
  if (!token) return null
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.id ?? payload.userId ?? payload.sub ?? null
  } catch {
    return null
  }
}

export async function refreshAccessToken(): Promise<string> {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) throw new Error('No refresh token');
  const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/auth/refresh-token`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  });
  const text = await res.text();
  if (!res.ok) {
    throw new Error(text);
  }
  const data = JSON.parse(text);
  if (!data.accessToken) throw new Error('Invalid refresh response');
  localStorage.setItem('token', data.accessToken);
  if (data.refreshToken) localStorage.setItem('refreshToken', data.refreshToken);
  return data.accessToken;
}