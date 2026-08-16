// Spring Boot API가 준비되면 window.CAREERLOG_API_BASE_URL을 설정하세요.
// 예: window.CAREERLOG_API_BASE_URL = 'http://localhost:8080/api'
const BASE_URL = window.CAREERLOG_API_BASE_URL ?? ''

async function request(path, options = {}) {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  })
  if (!response.ok) throw new Error(`API request failed: ${response.status}`)
  return response.status === 204 ? null : response.json()
}

export const api = {
  getNotes: (from, to) => request(`/api/career-notes?from=${from}&to=${to}`),
  createNote: (payload) => request('/api/career-notes', { method: 'POST', body: JSON.stringify(payload) }),
  updateNote: (id, payload) => request(`/api/career-notes/${id}`, { method: 'PATCH', body: JSON.stringify(payload) }),
  deleteNote: (id) => request(`/api/career-notes/${id}`, { method: 'DELETE' }),
  getReferences: (from, to) => request(`/api/references?from=${from}&to=${to}`),
  getJobRecommendations: (from, to) => request(`/api/job-recommendations?from=${from}&to=${to}`),
  startPreparation: (jobPostingId, payload) => request(`/api/job-postings/${jobPostingId}/action-plan`, { method: 'POST', body: JSON.stringify(payload) }),
  updateChecklist: (itemId, completed) => request(`/api/checklist-items/${itemId}`, { method: 'PATCH', body: JSON.stringify({ completed }) }),
}
