let csrfToken = null

async function ensureCsrf() {
  if (csrfToken) return csrfToken
  const response = await fetch('/api/auth/csrf', { credentials: 'same-origin' })
  if (!response.ok) throw new Error('보안 토큰을 가져오지 못했습니다.')
  const data = await response.json()
  csrfToken = data.token
  return csrfToken
}

async function request(path, options = {}) {
  const method = options.method ?? 'GET'
  const headers = { ...options.headers }
  if (options.body) headers['Content-Type'] = 'application/json'
  if (!['GET', 'HEAD'].includes(method)) headers['X-XSRF-TOKEN'] = await ensureCsrf()

  const response = await fetch(path, { ...options, method, headers, credentials: 'same-origin' })
  if (!response.ok) {
    const error = new Error(`API 요청 실패 (${response.status})`)
    error.status = response.status
    throw error
  }
  return response.status === 204 ? null : response.json()
}

export const api = {
  init: ensureCsrf,
  me: () => request('/api/auth/me'),
  signup: (payload) => request('/api/auth/signup', { method: 'POST', body: JSON.stringify(payload) }),
  login: (payload) => request('/api/auth/login', { method: 'POST', body: JSON.stringify(payload) }),
  logout: () => request('/api/auth/logout', { method: 'POST' }),
  getNotes: (from, to) => request(`/api/career-notes?from=${from}&to=${to}`),
  createNote: (payload) => request('/api/career-notes', { method: 'POST', body: JSON.stringify(payload) }),
  updateNote: (id, payload) => request(`/api/career-notes/${id}`, { method: 'PUT', body: JSON.stringify(payload) }),
  deleteNote: (id) => request(`/api/career-notes/${id}`, { method: 'DELETE' }),
  getProfile: () => request('/api/career-profile'),
  searchJobs: (from, to) => request('/api/job-recommendations/search', {
    method: 'POST', body: JSON.stringify({ from, to })
  }),
  createActionPlan: (jobPostingId) => request(`/api/job-postings/${jobPostingId}/action-plan`, { method: 'POST' }),
  getActionPlans: () => request('/api/action-plans'),
  getCalendar: (from, to) => request(`/api/action-plans/calendar?from=${from}&to=${to}`),
  updateChecklist: (itemId, completed) => request(`/api/checklist-items/${itemId}`, {
    method: 'PATCH', body: JSON.stringify({ completed })
  }),
  deleteChecklist: (itemId) => request(`/api/checklist-items/${itemId}`, { method: 'DELETE' }),
  deleteActionPlan: (actionPlanId) => request(`/api/action-plans/${actionPlanId}`, { method: 'DELETE' }),
}
