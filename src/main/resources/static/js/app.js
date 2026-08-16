import { jobs as mockJobs, references } from './mock-data.js'
import { api } from './api.js'

const currentMonth = new Date().toLocaleDateString('en-CA').slice(0, 7)
const state = { view: 'calendar', calendarMonth: currentMonth, notes: {}, jobs: structuredClone(mockJobs), selectedJobId: 'ux', checklistItems: [], deadlines: [], actionPlans: [], range: null, user: null, pendingChecklist: new Set() }
const $ = (selector) => document.querySelector(selector)
const dateKey = (day) => `${state.calendarMonth}-${String(day).padStart(2, '0')}`
const shortDate = (date) => date.slice(5).replace('-', '/')
const escapeHtml = (value = '') => value.replace(/[&<>'"]/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' })[char])
const selectedJob = () => state.jobs.find((job) => job.id === state.selectedJobId)
const actionTasks = () => state.checklistItems.map((item) => ({
  id: item.id, date: item.dueDate, title: item.title,
  color: item.actionPlanId % 2 === 0 ? 'blue' : 'red', completed: item.completed
}))

function showView(view) {
  state.view = view
  document.querySelectorAll('.view').forEach((section) => section.classList.toggle('active', section.id === `${view}View`))
  document.querySelectorAll('.nav').forEach((button) => button.classList.toggle('active', button.dataset.view === view))
  const month = Number(state.calendarMonth.slice(5))
  $('#pageTitle').textContent = { calendar: `${month}월의 취준 캘린더`, references: '나를 위한 탐색 레퍼런스', jobs: '지원할 공고와 준비 계획' }[view]
}

function renderCalendar() {
  const tasks = actionTasks()
  const [year, month] = state.calendarMonth.split('-').map(Number)
  const firstWeekday = new Date(year, month - 1, 1).getDay()
  const daysInMonth = new Date(year, month, 0).getDate()
  const previousDays = new Date(year, month - 1, 0).getDate()
  const previousMonth = Array.from({ length: firstWeekday }, (_, index) => previousDays - firstWeekday + index + 1)
    .map((day) => `<div class="day muted"><time>${day}</time></div>`).join('')
  const today = new Date().toLocaleDateString('en-CA')
  const days = Array.from({ length: daysInMonth }, (_, index) => index + 1).map((day) => {
    const key = dateKey(day); const note = state.notes[key]; const dayTasks = tasks.filter((task) => task.date === key); const deadlines = state.deadlines.filter((item) => item.deadline === key)
    return `<div class="day ${key === today ? 'today' : ''}" data-date="${key}"><time>${day}</time>${note ? `<button class="event note" data-note="${key}">▣ 취준노트</button>` : ''}${dayTasks.map((task) => `<div class="checklist-row event ${task.color} ${task.completed ? 'done' : ''}"><button type="button" class="check-toggle" data-task="${task.id}" role="checkbox" aria-checked="${task.completed}" ${state.pendingChecklist.has(String(task.id)) ? 'disabled' : ''}><span aria-hidden="true">${task.completed ? '☑' : '☐'}</span> ${escapeHtml(task.title)}</button><button type="button" class="delete-checklist" data-delete-task="${task.id}" title="체크리스트 삭제" aria-label="${escapeHtml(task.title)} 삭제">×</button></div>`).join('')}${deadlines.map((item) => `<div class="checklist-row event red" title="채용 마감"><span class="deadline-label">◆ ${escapeHtml(item.companyName)} 마감</span><button type="button" class="delete-checklist" data-delete-calendar-plan="${item.actionPlanId}" title="지원 공고 삭제" aria-label="${escapeHtml(item.companyName)} 지원 공고 삭제">×</button></div>`).join('')}</div>`
  }).join('')
  $('#calendar').innerHTML = previousMonth + days
  const done = tasks.filter((task) => task.completed).length
  $('#calendarStatus').textContent = tasks.length ? `준비 항목 ${done} / ${tasks.length} 완료` : '아직 진행 중인 지원 준비가 없어요'
}

function renderReferences() {
  $('#referenceGrid').innerHTML = references.map((reference, index) => `<article><small>${reference.type}</small><h3>${reference.title}</h3><p>${reference.reason}</p><button class="text add-reference" data-reference="${index}">탐색 일정에 추가하기 →</button></article>`).join('')
}

function renderJobs() {
  $('#jobList').innerHTML = state.jobs.map((job) => `<button class="job-row ${job.id === state.selectedJobId ? 'active' : ''}" data-job="${job.id}"><small>${job.company}</small><b>${job.title}</b><span>마감 ${shortDate(job.deadline)}</span></button>`).join('')
  const job = selectedJob()
  if (!job) return
  const searchableJob = /^\d+$/.test(String(job.id))
  const plan = state.actionPlans.find((item) => String(item.jobPostingId) === String(job.id))
  $('#jobDetail').innerHTML = `<p class="deadline">마감 ${job.deadline.replaceAll('-', '.')}</p><h2>${job.company} ${job.title}</h2><h4>공고가 요구하는 것</h4><ul>${job.requirements.map((item) => `<li>✓ ${item}</li>`).join('')}</ul><div class="match"><b>내 취준노트와 연결되는 점</b>${job.match}</div><div class="detail-footer"><small>${searchableJob ? '등록을 선택하면 마감일과 매일의 준비 항목이 캘린더에 추가돼요.' : '취준노트 기간을 선택해 실제 추천 공고를 먼저 불러와 주세요.'}</small><div>${plan ? `<button class="danger delete-job" data-plan="${plan.id}">지원 공고 삭제</button> ` : ''}<button class="primary ${searchableJob ? 'start-job' : 'search-job'}" data-job="${job.id}">${searchableJob ? (plan ? '캘린더에서 보기' : '지원 준비 등록') : '공고 탐색 먼저'} →</button></div></div>`
  $('#sideJobList').innerHTML = state.jobs.map((job) => `<button class="side-card job" data-side-job="${job.id}"><small>${job.company}</small><b>${job.title}</b></button>`).join('')
}

function fromJobResponse(job, index) {
  return {
    id: String(job.jobPostingId), company: job.companyName, title: job.title,
    deadline: job.deadline, color: index % 2 === 0 ? 'red' : 'blue',
    requirements: (job.description ?? '').split(',').map((item) => item.trim()).filter(Boolean),
    match: job.recommendationReason, url: job.url, tasks: []
  }
}

function openPreparationModal() {
  const root = $('#modalRoot')
  root.innerHTML = `<div class="backdrop"><section class="modal"><button class="close close-modal">×</button><p class="eyebrow">PHASE 3 · APPLY</p><h2>지원 준비 시작</h2><p class="modal-lead">선택한 기간의 취준노트를 바탕으로 적합한 공고를 찾아드려요.</p><div class="range-fields"><label>취준노트 탐색 시작일<input type="date" id="rangeStart" value="2026-08-01"></label><label>취준노트 탐색 종료일<input type="date" id="rangeEnd" value="2026-08-31"></label></div><p class="auth-error" id="prepError"></p><div class="modal-actions"><button class="text close-modal">취소</button><button class="primary run-preparation">채용 공고 탐색 시작! →</button></div></section></div>`
  $('.run-preparation').onclick = async () => {
    const start = $('#rangeStart').value; const end = $('#rangeEnd').value
    if (!start || !end || start > end) return $('#prepError').textContent = '탐색 기간을 확인해주세요.'
    const button = $('.run-preparation'); button.disabled = true; button.textContent = '내 노트 분석 중...'
    try {
      const result = await api.searchJobs(start, end)
      state.range = { start, end }
      state.jobs = result.map(fromJobResponse)
      state.selectedJobId = state.jobs[0]?.id ?? null
      root.innerHTML = ''; renderJobs(); showView('jobs')
    } catch (error) {
      $('#prepError').textContent = error.status === 400
        ? '선택한 기간에 취준노트가 없습니다.' : '공고를 불러오지 못했습니다.'
      button.disabled = false; button.textContent = '채용 공고 탐색 시작! →'
    }
  }
  root.querySelectorAll('.close-modal').forEach((button) => button.onclick = () => root.innerHTML = '')
}

function toDraft(note) {
  return {
    id: note.id,
    whatDidYouDo: note.whatDidYouDo ?? '',
    memorablePoint: note.memorablePoint ?? '',
    inputReason: note.inputReason ?? '',
    analysis: {
      experience: note.experience,
      activities: note.activities ?? [],
      reaction: note.reaction,
      reason: note.reason
    }
  }
}

async function loadNotes() {
  const { from, to } = monthRange()
  const notes = await api.getNotes(from, to)
  state.notes = Object.fromEntries(notes.map((note) => [note.noteDate, toDraft(note)]))
  renderCalendar()
}

async function loadActionCalendar() {
  const { from, to } = monthRange()
  const calendar = await api.getCalendar(from, to)
  state.checklistItems = calendar.checklistItems
  state.deadlines = calendar.deadlines
  renderCalendar(); renderJobs()
}

async function loadActionPlans() {
  state.actionPlans = await api.getActionPlans()
  renderJobs()
}

function monthRange() {
  const [year, month] = state.calendarMonth.split('-').map(Number)
  const lastDay = new Date(year, month, 0).getDate()
  return { from: `${state.calendarMonth}-01`, to: `${state.calendarMonth}-${String(lastDay).padStart(2, '0')}` }
}

async function loadMonth() {
  await Promise.all([loadNotes(), loadActionCalendar(), loadActionPlans()])
  showView('calendar')
}

async function moveMonth(offset) {
  const [year, month] = state.calendarMonth.split('-').map(Number)
  const target = new Date(year, month - 1 + offset, 1)
  state.calendarMonth = `${target.getFullYear()}-${String(target.getMonth() + 1).padStart(2, '0')}`
  await loadMonth()
}

function openActionPlanModal(job) {
  const existing = state.actionPlans.find((item) => String(item.jobPostingId) === String(job.id))
  if (existing) return showView('calendar')
  const root = $('#modalRoot')
  root.innerHTML = `<div class="backdrop"><section class="modal"><button class="close close-modal">×</button><p class="eyebrow">ACTION PLAN</p><h2>${escapeHtml(job.company)} ${escapeHtml(job.title)}</h2><p class="modal-lead">이 공고와 준비 체크리스트를 내 캘린더에 등록할까요?</p><div class="job-preview"><span class="red-dot"></span><div><small>마감 ${job.deadline}</small><b>오늘부터 마감일까지 매일 한 가지</b><p>공고 분석, 경험 정리, 이력서·자기소개서 보완, 최종 제출 순서로 생성됩니다.</p></div></div><p class="auth-error" id="planError"></p><div class="modal-actions"><button class="text close-modal">등록하지 않기</button><button class="primary confirm-plan">공고와 체크리스트 등록 →</button></div></section></div>`
  $('.confirm-plan').onclick = async () => {
    const button = $('.confirm-plan'); button.disabled = true; button.textContent = '체크리스트 생성 중...'
    try {
      await api.createActionPlan(job.id)
      state.calendarMonth = job.deadline.slice(0, 7)
      root.innerHTML = ''; await loadMonth()
    } catch (error) { $('#planError').textContent = '체크리스트를 생성하지 못했습니다.'; button.disabled = false }
  }
  root.querySelectorAll('.close-modal').forEach((button) => button.onclick = () => root.innerHTML = '')
}

function openNoteModal(date, draft = state.notes[date]) {
  const existing = Boolean(draft); let editing = !existing
  const root = $('#modalRoot'); const value = (key) => escapeHtml(draft?.[key] ?? '')
  const analysis = draft?.analysis
  const analysisHtml = existing && analysis?.experience ? `<div class="match"><b>AI 구조화 결과</b><p><strong>경험</strong> ${escapeHtml(analysis.experience ?? '')}</p><p><strong>구체 활동</strong> ${(analysis.activities ?? []).map(escapeHtml).join(', ') || '-'}</p><p><strong>생각·반응</strong> ${escapeHtml(analysis.reaction ?? '-') }</p><p><strong>작성한 이유</strong> ${escapeHtml(analysis.reason ?? '-')}</p></div>` : ''
  root.innerHTML = `<div class="backdrop"><section class="modal"><button class="close close-modal">×</button><p class="eyebrow">${date.replaceAll('-', '.')}</p><h2 id="noteModalTitle">${existing ? '취준노트' : '오늘의 취준노트'}</h2><p class="modal-lead">기록한 내용만 바탕으로 AI가 경험을 구조화해요.</p><label>오늘 취준과 관련해서 무엇을 했거나 접했나요?<textarea id="whatDidYouDo">${value('whatDidYouDo')}</textarea></label><label>그중 어떤 점이 가장 기억에 남았나요?<textarea id="memorablePoint">${value('memorablePoint')}</textarea></label><label>왜 그렇게 느꼈던 것 같나요? <small>(선택)</small><textarea id="inputReason">${value('inputReason')}</textarea></label>${analysisHtml}<div class="modal-actions"><button class="danger delete-note" ${existing ? '' : 'hidden'}>삭제</button><span class="spacer"></span><button class="text close-modal">취소</button><button class="outline edit-note" ${existing ? '' : 'hidden'}>취준노트 수정</button><button class="primary save-note">취준노트 저장 →</button></div></section></div>`
  const fields = [$('#whatDidYouDo'), $('#memorablePoint'), $('#inputReason')]
  const applyMode = () => { fields.forEach((field) => field.disabled = !editing); $('.save-note').hidden = !editing; $('.edit-note').hidden = !existing || editing }
  applyMode()
  $('.edit-note').onclick = () => { editing = true; applyMode() }
  $('.save-note').onclick = async () => {
    const button = $('.save-note'); button.disabled = true; button.textContent = 'AI 분석 중...'
    const payload = { whatDidYouDo: $('#whatDidYouDo').value, memorablePoint: $('#memorablePoint').value, reason: $('#inputReason').value || null, noteDate: date }
    try {
      if (!payload.whatDidYouDo.trim() || !payload.memorablePoint.trim()) return alert('첫 번째와 두 번째 질문에 답해주세요.')
      if (existing) await api.updateNote(draft.id, payload); else await api.createNote(payload)
      root.innerHTML = ''; await loadNotes()
    } catch (error) { alert(error.message) } finally { button.disabled = false }
  }
  $('.delete-note').onclick = async () => {
    if (!confirm('이 취준노트를 삭제할까요?')) return
    try { await api.deleteNote(draft.id); root.innerHTML = ''; await loadNotes() } catch (error) { alert(error.message) }
  }
  root.querySelectorAll('.close-modal').forEach((button) => button.onclick = () => root.innerHTML = '')
}

function showAuthModal() {
  const root = $('#modalRoot')
  root.innerHTML = `<style>.auth-help{color:#777;font-size:12px;margin-top:-10px}.auth-error{color:#c33;min-height:20px}</style><div class="backdrop"><section class="modal"><p class="eyebrow">CAREERLOG</p><h2>로그인</h2><p class="modal-lead">나만의 취준 기록을 시작해보세요.</p><label>아이디<input id="loginId" autocomplete="username" placeholder="2자 이상"></label><p class="auth-help">한글, 영문, 숫자, 점, 밑줄, 하이픈을 사용할 수 있어요.</p><label>비밀번호<input id="password" type="password" autocomplete="current-password" placeholder="8자 이상"></label><p class="auth-error" id="authError"></p><div class="modal-actions"><button class="outline" id="signupButton">회원가입</button><button class="primary" id="loginButton">로그인 →</button></div></section></div>`
  const credentials = () => ({ loginId: $('#loginId').value, password: $('#password').value })
  const run = async (signup) => {
    $('#authError').textContent = ''
    const input = credentials()
    if (input.loginId.trim().length < 2) return $('#authError').textContent = '아이디를 2자 이상 입력해주세요.'
    if (input.password.length < 8) return $('#authError').textContent = '비밀번호를 8자 이상 입력해주세요.'
    try {
      if (signup) await api.signup(input)
      state.user = await api.login(input)
      root.innerHTML = ''; applyUser(); await loadMonth()
    } catch (error) {
      $('#authError').textContent = signup && error.status === 409
        ? '이미 사용 중인 아이디입니다.'
        : signup ? '회원가입 정보를 확인해주세요.' : '아이디와 비밀번호를 확인해주세요.'
    }
  }
  $('#loginButton').onclick = () => run(false)
  $('#signupButton').onclick = () => run(true)
}

function applyUser() {
  const profile = document.querySelector('.profile div')
  if (profile && state.user) profile.innerHTML = `<b>${escapeHtml(state.user.loginId)}</b><p>내 커리어 탐색</p>`
}

document.addEventListener('click', (event) => {
  const view = event.target.closest('[data-view]'); if (view) showView(view.dataset.view)
  const day = event.target.closest('.day[data-date]'); if (day && !event.target.closest('button')) openNoteModal(day.dataset.date)
  const note = event.target.closest('[data-note]'); if (note) openNoteModal(note.dataset.note)
  const deleteTask = event.target.closest('[data-delete-task]'); if (deleteTask) {
    event.preventDefault(); event.stopPropagation()
    if (confirm('이 체크리스트를 삭제할까요?')) {
      api.deleteChecklist(deleteTask.dataset.deleteTask).then(loadActionCalendar).catch((error) => alert(error.message))
    }
  }
  const deleteCalendarPlan = event.target.closest('[data-delete-calendar-plan]'); if (deleteCalendarPlan) {
    event.preventDefault(); event.stopPropagation()
    if (confirm('이 지원 공고를 삭제할까요? 관련 체크리스트도 모두 삭제됩니다.')) {
      api.deleteActionPlan(deleteCalendarPlan.dataset.deleteCalendarPlan).then(loadMonth).catch((error) => alert(error.message))
    }
  }
  const task = event.target.closest('[data-task]'); if (task) {
    event.preventDefault(); event.stopPropagation()
    const item = state.checklistItems.find((candidate) => String(candidate.id) === task.dataset.task)
    if (item && !state.pendingChecklist.has(String(item.id))) {
      const itemId = String(item.id)
      state.pendingChecklist.add(itemId); renderCalendar()
      api.updateChecklist(item.id, !item.completed).then((updated) => {
        const target = state.checklistItems.find((candidate) => String(candidate.id) === String(updated.id))
        if (target) target.completed = updated.completed
      }).catch((error) => alert(error.message)).finally(() => {
        state.pendingChecklist.delete(itemId); renderCalendar()
      })
    }
  }
  const job = event.target.closest('[data-job]'); if (job) { state.selectedJobId = job.dataset.job; renderJobs() }
  const sideJob = event.target.closest('[data-side-job]'); if (sideJob) { state.selectedJobId = sideJob.dataset.sideJob; renderJobs(); showView('jobs') }
  const startJob = event.target.closest('.start-job'); if (startJob) openActionPlanModal(selectedJob())
  const searchJob = event.target.closest('.search-job'); if (searchJob) openPreparationModal()
  const deleteJob = event.target.closest('.delete-job'); if (deleteJob) {
    const job = selectedJob()
    if (confirm('이 지원 공고를 삭제할까요? 연결된 체크리스트도 모두 삭제됩니다.')) {
      api.deleteActionPlan(deleteJob.dataset.plan).then(async () => {
        state.jobs = state.jobs.filter((item) => item.id !== job.id)
        state.selectedJobId = state.jobs[0]?.id ?? null
        await loadMonth()
      }).catch((error) => alert(error.message))
    }
  }
  if (event.target.closest('.month-prev')) moveMonth(-1)
  if (event.target.closest('.month-next')) moveMonth(1)
})

$('#openPrep').onclick = openPreparationModal

async function start() {
  document.head.insertAdjacentHTML('beforeend', '<style>.checklist-row{display:flex!important;align-items:flex-start;padding:0!important}.check-toggle,.deadline-label{flex:1;border:0;background:transparent;color:inherit;text-align:left;padding:6px;font:inherit;cursor:pointer}.deadline-label{cursor:default}.delete-checklist{flex:0 0 auto;border:0;background:transparent;color:#a84d48;padding:4px 6px;font-size:16px;line-height:1;cursor:pointer}.delete-checklist:hover{background:rgba(168,77,72,.12)}.checklist-row.done .check-toggle{text-decoration:line-through}</style>')
  $('#calendarStatus').insertAdjacentHTML('beforebegin', '<span class="month-nav"><button class="outline month-prev">‹</button> <button class="outline month-next">›</button></span>')
  renderCalendar(); renderReferences(); renderJobs()
  await api.init()
  try { state.user = await api.me(); applyUser(); await loadMonth() } catch (error) { if (error.status === 401) showAuthModal(); else alert(error.message) }
}

start()
