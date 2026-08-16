import { jobs, references, initialNotes } from './mock-data.js'

const state = { view: 'calendar', notes: structuredClone(initialNotes), activeJobId: null, selectedJobId: 'ux', completed: {}, range: null }
const $ = (selector) => document.querySelector(selector)
const dateKey = (day) => `2026-08-${String(day).padStart(2, '0')}`
const shortDate = (date) => date.slice(5).replace('-', '/')
const activeJob = () => jobs.find((job) => job.id === state.activeJobId)
const selectedJob = () => jobs.find((job) => job.id === state.selectedJobId)
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
  $('#calendarStatus').textContent = tasks.length ? `${state.range ? `노트 반영 ${shortDate(state.range.start)}–${shortDate(state.range.end)} · ` : ''}준비 항목 ${done} / ${tasks.length} 완료` : '아직 진행 중인 지원 준비가 없어요'
}

function renderReferences() {
  $('#referenceGrid').innerHTML = references.map((reference, index) => `<article><small>${reference.type}</small><h3>${reference.title}</h3><p>${reference.reason}</p><button class="text add-reference" data-reference="${index}">탐색 일정에 추가하기 →</button></article>`).join('')
}

function renderJobs() {
  $('#jobList').innerHTML = jobs.map((job) => `<button class="job-row ${job.id === state.selectedJobId ? 'active' : ''}" data-job="${job.id}"><small>${job.company}</small><b>${job.title}</b><span>마감 ${shortDate(job.deadline)} · ${job.id === 'ux' ? 'D-14' : 'D-20'}</span></button>`).join('')
  const job = selectedJob()
  $('#jobDetail').innerHTML = `<p class="deadline">마감 ${job.deadline.replaceAll('-', '.')} · ${job.id === 'ux' ? 'D-14' : 'D-20'}</p><h2>${job.company} ${job.title}</h2><h4>공고가 요구하는 것</h4><ul>${job.requirements.map((item) => `<li>✓ ${item}</li>`).join('')}</ul><div class="match"><b>내 취준노트와 연결되는 점</b>${job.match}</div><div class="detail-footer"><small>준비 계획은 마감일을 기준으로 캘린더에 배치돼요.</small><button class="primary start-job" data-job="${job.id}">${state.activeJobId === job.id ? '캘린더에서 보기' : '지원 준비 시작'} →</button></div>`
  $('#sideJobList').innerHTML = jobs.map((job) => `<button class="side-card job ${state.activeJobId === job.id ? `selected ${job.color}` : ''}" data-side-job="${job.id}"><small>${job.id === 'ux' ? 'D-14' : 'D-20'} · ${job.company}</small><b>${job.title}</b></button>`).join('')
}

function openNoteModal(date, draft = state.notes[date]) {
  const existing = Boolean(draft); let editing = !existing
  const root = $('#modalRoot'); const value = (key) => draft?.[key] ?? ''
  root.innerHTML = `<div class="backdrop"><section class="modal"><button class="close close-modal">×</button><p class="eyebrow">${date.replaceAll('-', '.')}</p><h2 id="noteModalTitle">${existing ? '취준노트' : '오늘의 취준노트'}</h2><p class="modal-lead">작은 탐색도 다음 선택의 근거가 됩니다.</p><label>오늘 한 탐색<textarea id="exploration">${value('exploration')}</textarea></label><label>기억에 남는 장면<textarea id="scene">${value('scene')}</textarea></label><label>나의 반응<textarea id="reaction">${value('reaction')}</textarea></label><label>다음에 확인할 것<input id="question" value="${value('question')}"></label><div class="modal-actions"><button class="danger delete-note" ${existing ? '' : 'hidden'}>삭제</button><span class="spacer"></span><button class="text close-modal">취소</button><button class="outline edit-note" ${existing ? '' : 'hidden'}>수정하기</button><button class="primary save-note">취준노트 저장 →</button></div></section></div>`
  const fields = ['exploration', 'scene', 'reaction', 'question'].map((id) => $(`#${id}`)); const applyMode = () => { fields.forEach((field) => field.disabled = !editing); $('.save-note').hidden = !editing; $('.edit-note').hidden = !existing || editing; $('#noteModalTitle').textContent = existing && !editing ? '취준노트' : '오늘의 취준노트' }; applyMode()
  $('.edit-note').onclick = () => { editing = true; applyMode() }
  $('.save-note').onclick = () => { state.notes[date] = { id: draft?.id ?? crypto.randomUUID(), exploration: $('#exploration').value, scene: $('#scene').value, reaction: $('#reaction').value, question: $('#question').value }; root.innerHTML = ''; renderCalendar() }
  $('.delete-note').onclick = () => { if (confirm('이 취준노트를 삭제할까요?')) { delete state.notes[date]; root.innerHTML = ''; renderCalendar() } }
  root.querySelectorAll('.close-modal').forEach((button) => button.onclick = () => root.innerHTML = '')
}

function openPreparationModal() {
  const root = $('#modalRoot'); root.innerHTML = `<div class="backdrop"><section class="modal"><button class="close close-modal">×</button><p class="eyebrow">PHASE 3 · APPLY</p><h2>지원 준비 시작</h2><p class="modal-lead">이 기간의 취준노트와 탐색 레퍼런스를 반영해 공고와 Action Plan을 준비합니다.</p><div class="range-fields"><label>취준노트 탐색 시작일<input type="date" id="rangeStart" value="2026-08-10"></label><label>취준노트 탐색 종료일<input type="date" id="rangeEnd" value="2026-08-16"></label></div><div class="job-preview"><span class="red-dot"></span><div><small>탐색 결과 예시</small><b>A회사 UX 리서처 인턴 · 마감 D-14</b><p>사용자 관찰과 정보 구조화 경험이 반복적으로 나타났어요.</p></div></div><div class="modal-actions"><button class="text close-modal">취소</button><button class="primary run-preparation">채용 공고 탐색 &amp; 지원 준비 시작! →</button></div></section></div>`
  $('.run-preparation').onclick = () => { const start = $('#rangeStart').value; const end = $('#rangeEnd').value; if (!start || !end || start > end) return alert('탐색 기간을 확인해주세요.'); state.range = { start, end }; state.activeJobId = 'ux'; state.selectedJobId = 'ux'; root.innerHTML = ''; renderCalendar(); renderJobs(); showView('calendar') }
  root.querySelectorAll('.close-modal').forEach((button) => button.onclick = () => root.innerHTML = '')
}

document.addEventListener('click', (event) => {
  const view = event.target.closest('[data-view]'); if (view) showView(view.dataset.view)
  const day = event.target.closest('.day[data-date]'); if (day && !event.target.closest('button')) openNoteModal(day.dataset.date)
  const note = event.target.closest('[data-note]'); if (note) openNoteModal(note.dataset.note)
  const task = event.target.closest('[data-task]'); if (task) { event.stopPropagation(); state.completed[task.dataset.task] = !state.completed[task.dataset.task]; renderCalendar() }
  const job = event.target.closest('[data-job]'); if (job) { state.selectedJobId = job.dataset.job; renderJobs() }
  const sideJob = event.target.closest('[data-side-job]'); if (sideJob) { state.selectedJobId = sideJob.dataset.sideJob; renderJobs(); showView('jobs') }
  const start = event.target.closest('.start-job'); if (start) { state.activeJobId = start.dataset.job; renderCalendar(); renderJobs(); showView('calendar') }
  const reference = event.target.closest('.add-reference'); if (reference) openNoteModal('2026-08-16', { exploration: `[레퍼런스 탐색] ${references[reference.dataset.reference].title}`, scene: '', reaction: '', question: '' })
})

$('#openPrep').onclick = openPreparationModal
renderCalendar(); renderReferences(); renderJobs()
