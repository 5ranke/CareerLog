import { jobs as mockJobs, references } from './mock-data.js'
import { api } from './api.js'

const state = { view: 'calendar', notes: {}, jobs: structuredClone(mockJobs), activeJobId: null, selectedJobId: 'ux', completed: {}, range: null, user: null }
const $ = (selector) => document.querySelector(selector)
const dateKey = (day) => `2026-08-${String(day).padStart(2, '0')}`
const shortDate = (date) => date.slice(5).replace('-', '/')
const escapeHtml = (value = '') => value.replace(/[&<>'"]/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' })[char])
const activeJob = () => state.jobs.find((job) => job.id === state.activeJobId)
const selectedJob = () => state.jobs.find((job) => job.id === state.selectedJobId)
const actionTasks = () => activeJob() ? activeJob().tasks.map(([date, title], index) => ({ id: `${activeJob().id}-${index}`, date, title, color: activeJob().color })) : []

function showView(view) {
  state.view = view
  document.querySelectorAll('.view').forEach((section) => section.classList.toggle('active', section.id === `${view}View`))
  document.querySelectorAll('.nav').forEach((button) => button.classList.toggle('active', button.dataset.view === view))
  $('#pageTitle').textContent = { calendar: '8월의 취준 캘린더', references: '나를 위한 탐색 레퍼런스', jobs: '지원할 공고와 준비 계획' }[view]
}

function renderCalendar() {
  const tasks = actionTasks()
  const previousMonth = [26, 27, 28, 29, 30, 31].map((day) => `<div class="day muted"><time>${day}</time></div>`).join('')
  const days = Array.from({ length: 31 }, (_, index) => index + 1).map((day) => {
    const key = dateKey(day); const note = state.notes[key]; const dayTasks = tasks.filter((task) => task.date === key)
    return `<div class="day ${day === 16 ? 'today' : ''}" data-date="${key}"><time>${day}</time>${note ? `<button class="event note" data-note="${key}">▣ 취준노트</button>` : ''}${dayTasks.map((task) => `<button class="event ${task.color} ${state.completed[task.id] ? 'done' : ''}" data-task="${task.id}"><input type="checkbox" ${state.completed[task.id] ? 'checked' : ''}>${task.title}</button>`).join('')}</div>`
  }).join('')
  $('#calendar').innerHTML = previousMonth + days
  const done = tasks.filter((task) => state.completed[task.id]).length
  $('#calendarStatus').textContent = tasks.length ? `준비 항목 ${done} / ${tasks.length} 완료` : '아직 진행 중인 지원 준비가 없어요'
}

function renderReferences() {
  $('#referenceGrid').innerHTML = references.map((reference, index) => `<article><small>${reference.type}</small><h3>${reference.title}</h3><p>${reference.reason}</p><button class="text add-reference" data-reference="${index}">탐색 일정에 추가하기 →</button></article>`).join('')
}

function renderJobs() {
  $('#jobList').innerHTML = state.jobs.map((job) => `<button class="job-row ${job.id === state.selectedJobId ? 'active' : ''}" data-job="${job.id}"><small>${job.company}</small><b>${job.title}</b><span>마감 ${shortDate(job.deadline)}</span></button>`).join('')
  const job = selectedJob()
  if (!job) return
  $('#jobDetail').innerHTML = `<p class="deadline">마감 ${job.deadline.replaceAll('-', '.')}</p><h2>${job.company} ${job.title}</h2><h4>공고가 요구하는 것</h4><ul>${job.requirements.map((item) => `<li>✓ ${item}</li>`).join('')}</ul><div class="match"><b>내 취준노트와 연결되는 점</b>${job.match}</div><div class="detail-footer"><small>준비 계획은 마감일을 기준으로 캘린더에 배치돼요.</small><button class="primary start-job" data-job="${job.id}">지원 준비 시작 →</button></div>`
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
  return { id: note.id, exploration: note.title ?? '', scene: note.content ?? '', reaction: note.aiSummary ?? '', question: '' }
}

async function loadNotes() {
  const notes = await api.getNotes('2026-08-01', '2026-08-31')
  state.notes = Object.fromEntries(notes.map((note) => [note.noteDate, toDraft(note)]))
  renderCalendar()
}

function openNoteModal(date, draft = state.notes[date]) {
  const existing = Boolean(draft); let editing = !existing
  const root = $('#modalRoot'); const value = (key) => escapeHtml(draft?.[key] ?? '')
  root.innerHTML = `<div class="backdrop"><section class="modal"><button class="close close-modal">×</button><p class="eyebrow">${date.replaceAll('-', '.')}</p><h2 id="noteModalTitle">${existing ? '취준노트' : '오늘의 취준노트'}</h2><p class="modal-lead">작은 탐색도 다음 선택의 근거가 됩니다.</p><label>오늘 한 탐색<textarea id="exploration">${value('exploration')}</textarea></label><label>상세 기록<textarea id="scene">${value('scene')}</textarea></label><label>AI 요약<textarea id="reaction" disabled>${value('reaction')}</textarea></label><div class="modal-actions"><button class="danger delete-note" ${existing ? '' : 'hidden'}>삭제</button><span class="spacer"></span><button class="text close-modal">취소</button><button class="outline edit-note" ${existing ? '' : 'hidden'}>수정하기</button><button class="primary save-note">취준노트 저장 →</button></div></section></div>`
  const fields = [$('#exploration'), $('#scene')]
  const applyMode = () => { fields.forEach((field) => field.disabled = !editing); $('.save-note').hidden = !editing; $('.edit-note').hidden = !existing || editing }
  applyMode()
  $('.edit-note').onclick = () => { editing = true; applyMode() }
  $('.save-note').onclick = async () => {
    const button = $('.save-note'); button.disabled = true; button.textContent = 'AI 분석 중...'
    const payload = { title: $('#exploration').value || '취준노트', content: $('#scene').value, noteDate: date }
    try {
      if (!payload.content.trim()) return alert('상세 기록을 입력해주세요.')
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
      root.innerHTML = ''; applyUser(); await loadNotes()
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
  const task = event.target.closest('[data-task]'); if (task) { state.completed[task.dataset.task] = !state.completed[task.dataset.task]; renderCalendar() }
  const job = event.target.closest('[data-job]'); if (job) { state.selectedJobId = job.dataset.job; renderJobs() }
  const sideJob = event.target.closest('[data-side-job]'); if (sideJob) { state.selectedJobId = sideJob.dataset.sideJob; renderJobs(); showView('jobs') }
})

$('#openPrep').onclick = openPreparationModal

async function start() {
  renderCalendar(); renderReferences(); renderJobs()
  await api.init()
  try { state.user = await api.me(); applyUser(); await loadNotes() } catch (error) { if (error.status === 401) showAuthModal(); else alert(error.message) }
}

start()
