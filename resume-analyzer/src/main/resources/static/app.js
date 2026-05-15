/* ════════════════════════════════════════
   STATE
════════════════════════════════════════ */
const S = {
  token: localStorage.getItem('ra_token'),
  email: localStorage.getItem('ra_email'),
  resumes: [],
  jobs: [],
  matches: [],
  selectedFile: null,
};

/* ════════════════════════════════════════
   API
════════════════════════════════════════ */
async function api(method, path, body = null, isForm = false) {
  const headers = {};
  if (S.token) headers['Authorization'] = `Bearer ${S.token}`;
  if (body && !isForm) headers['Content-Type'] = 'application/json';

  const res = await fetch(`/api${path}`, {
    method,
    headers,
    body: isForm ? body : (body ? JSON.stringify(body) : null),
  });

  if (res.status === 401) {
    // Token expired or invalid — clear session and send to login
    localStorage.removeItem('ra_token');
    localStorage.removeItem('ra_email');
    S.token = null; S.email = null;
    hide('appPage'); hide('landingPage'); show('authPage');
    switchAuthTab('login');
    document.getElementById('loginErr').textContent = 'Your session expired. Please sign in again.';
    document.getElementById('loginErr').classList.remove('hidden');
    return null;
  }

  const text = await res.text();
  let data;
  try { data = JSON.parse(text); } catch { data = text; }
  if (!res.ok) throw new Error(data?.message || data?.error || 'Something went wrong');
  return data;
}

/* ════════════════════════════════════════
   ROUTING
════════════════════════════════════════ */
function showLanding() {
  show('landingPage'); hide('authPage'); hide('appPage');
}
function showAuth(tab = 'login') {
  hide('landingPage'); show('authPage'); hide('appPage');
  switchAuthTab(tab);
}
function showApp() {
  hide('landingPage'); hide('authPage'); show('appPage');
  document.getElementById('sbUser').textContent = S.email;
  document.getElementById('welcomeHeading').textContent =
    `Welcome back, ${S.email.split('@')[0]} 👋`;
  navigate('dashboard');
}

function navigate(view) {
  document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
  document.querySelectorAll('.sb-link').forEach(l => l.classList.remove('active'));
  document.getElementById(`view-${view}`).classList.add('active');
  const sb = document.getElementById(`sb-${view}`);
  if (sb) sb.classList.add('active');
  const titles = { dashboard:'Dashboard', resumes:'My Resumes', jobs:'Jobs', matches:'My Matches' };
  document.getElementById('topbarTitle').textContent = titles[view] || '';
  if (view === 'dashboard') loadDashboard();
  if (view === 'resumes')   loadResumes();
  if (view === 'jobs')      loadJobs();
  if (view === 'matches')   loadMatches();
}

/* ════════════════════════════════════════
   AUTH
════════════════════════════════════════ */
function switchAuthTab(tab) {
  const isLogin = tab === 'login';
  document.getElementById('loginForm').classList.toggle('hidden', !isLogin);
  document.getElementById('registerForm').classList.toggle('hidden', isLogin);
  document.getElementById('tabLogin').classList.toggle('active', isLogin);
  document.getElementById('tabRegister').classList.toggle('active', !isLogin);
}

async function handleLogin(e) {
  e.preventDefault();
  const btn = document.getElementById('loginBtn');
  const err = document.getElementById('loginErr');
  err.classList.add('hidden');
  setLoading(btn, 'Signing in…');
  try {
    const data = await api('POST', '/auth/login', {
      email: document.getElementById('loginEmail').value,
      password: document.getElementById('loginPassword').value,
    });
    saveSession(data);
    showApp();
  } catch (ex) {
    err.textContent = ex.message; err.classList.remove('hidden');
  } finally { resetBtn(btn, 'Sign In'); }
}

async function handleRegister(e) {
  e.preventDefault();
  const btn = document.getElementById('registerBtn');
  const err = document.getElementById('registerErr');
  err.classList.add('hidden');
  setLoading(btn, 'Creating account…');
  try {
    const data = await api('POST', '/auth/register', {
      name: document.getElementById('regName').value,
      email: document.getElementById('regEmail').value,
      password: document.getElementById('regPassword').value,
    });
    saveSession(data);
    showApp();
  } catch (ex) {
    err.textContent = ex.message; err.classList.remove('hidden');
  } finally { resetBtn(btn, 'Create Account'); }
}

function saveSession(data) {
  S.token = data.token; S.email = data.email;
  localStorage.setItem('ra_token', data.token);
  localStorage.setItem('ra_email', data.email);
}

function logout() {
  S.token = null; S.email = null;
  localStorage.removeItem('ra_token'); localStorage.removeItem('ra_email');
  showLanding();
}

/* ════════════════════════════════════════
   DASHBOARD
════════════════════════════════════════ */
async function loadDashboard() {
  try {
    const [resumes, jobs, matches] = await Promise.all([
      api('GET', '/resume'),
      api('GET', '/jobs'),
      api('GET', '/matches'),
    ]);
    S.resumes = resumes || [];
    S.jobs    = jobs    || [];
    S.matches = matches || [];

    document.getElementById('kpiResumes').textContent  = S.resumes.length;
    document.getElementById('kpiJobs').textContent     = S.jobs.length;
    document.getElementById('kpiMatches').textContent  = S.matches.length;

    const best = S.matches.length
      ? Math.max(...S.matches.map(m => m.finalScore || 0)).toFixed(0) + '%'
      : '—';
    document.getElementById('kpiBest').textContent = best;

    // Mini resume list
    const rl = document.getElementById('dashResumeList');
    if (!S.resumes.length) {
      rl.innerHTML = '<div style="padding:1.5rem;text-align:center;color:var(--muted);font-size:.85rem">No resumes yet</div>';
    } else {
      rl.innerHTML = S.resumes.slice(0, 4).map(r => `
        <div class="mini-resume">
          <div class="mini-icon">📄</div>
          <div style="flex:1;overflow:hidden">
            <div class="mini-name">${esc(r.fileName)}</div>
            <div class="mini-sub">${(r.extractedSkills||[]).length} skills · ${fmtDate(r.uploadedAt)}</div>
          </div>
          <button class="btn-match" onclick="runMatch(${r.id},'${esc(r.fileName)}')">Match</button>
        </div>`).join('');
    }

    // Mini match list
    const ml = document.getElementById('dashMatchList');
    if (!S.matches.length) {
      ml.innerHTML = '<div style="padding:1.5rem;text-align:center;color:var(--muted);font-size:.85rem">No matches yet — upload a resume and click Match</div>';
    } else {
      ml.innerHTML = S.matches.slice(0, 5).map(m => {
        const lbl = label(m.finalScore);
        return `<div class="mini-match">
          <div style="flex:1">
            <div class="mini-match-title">${esc(m.jobTitle)}</div>
            <div class="mini-sub">${(m.finalScore||0).toFixed(1)}% match</div>
          </div>
          <span class="mini-badge badge-${lbl.toLowerCase()}">${lbl}</span>
        </div>`;
      }).join('');
    }
  } catch (ex) { toast(ex.message, 'error'); }
}

/* ════════════════════════════════════════
   RESUMES
════════════════════════════════════════ */
async function loadResumes() {
  const el = document.getElementById('resumeGrid');
  el.innerHTML = '<div class="empty"><div class="spinner" style="margin:0 auto 1rem"></div></div>';
  try {
    S.resumes = await api('GET', '/resume') || [];
    renderResumes();
  } catch (ex) { toast(ex.message, 'error'); }
}

function renderResumes() {
  const el = document.getElementById('resumeGrid');
  if (!S.resumes.length) {
    el.innerHTML = '<div class="empty">No resumes yet.<br/>Click "Upload Resume" to get started.</div>';
    return;
  }
  el.innerHTML = S.resumes.map(r => {
    const skills = r.extractedSkills || [];
    const shown  = skills.slice(0, 5);
    const extra  = skills.length - shown.length;
    return `
    <div class="resume-card">
      <div class="rc-head">
        <div class="rc-file-icon">📄</div>
        <div class="rc-meta">
          <h4>${esc(r.fileName)}</h4>
          <span>${fmtDate(r.uploadedAt)}</span>
        </div>
      </div>
      <div class="rc-skills">
        ${shown.map(s => `<span class="skill-pill">${esc(s.skillName)}</span>`).join('')}
        ${extra > 0 ? `<span class="skill-pill more">+${extra} more</span>` : ''}
        ${!skills.length ? '<span style="color:var(--muted);font-size:.78rem">No skills detected</span>' : ''}
      </div>
      <div class="rc-footer">
        <span class="rc-conf">${skills.length} skills extracted</span>
        <button class="btn-match" onclick="runMatch(${r.id},'${esc(r.fileName)}')">⚡ Match Jobs</button>
      </div>
    </div>`;
  }).join('');
}

/* ════════════════════════════════════════
   JOBS
════════════════════════════════════════ */
async function loadJobs() {
  const el = document.getElementById('jobGrid');
  el.innerHTML = '<div class="empty"><div class="spinner" style="margin:0 auto 1rem"></div></div>';
  try {
    S.jobs = await api('GET', '/jobs') || [];
    renderJobs();
  } catch (ex) { toast(ex.message, 'error'); }
}

function renderJobs() {
  const el = document.getElementById('jobGrid');
  if (!S.jobs.length) {
    el.innerHTML = '<div class="empty">No jobs posted yet.<br/>Click "Post a Job" to add one.</div>';
    return;
  }
  el.innerHTML = S.jobs.map(j => `
    <div class="job-card">
      <div class="jc-title">${esc(j.title)}</div>
      <div class="jc-desc">${esc(j.description || '')}</div>
      <div class="jc-skills">
        ${(j.jobSkills || []).map(js =>
          `<span class="${js.weight === 'MANDATORY' ? 'pill-m' : 'pill-o'}">
            ${esc(js.skill?.skillName || '')} ${js.weight === 'MANDATORY' ? '★' : '○'}
          </span>`
        ).join('')}
      </div>
    </div>`).join('');
}

/* ════════════════════════════════════════
   MATCHES
════════════════════════════════════════ */
async function loadMatches() {
  const el = document.getElementById('matchList');
  el.innerHTML = '<div class="loading-wrap"><div class="spinner"></div><p>Loading matches…</p></div>';
  try {
    S.matches = await api('GET', '/matches') || [];
    renderMatches(el, S.matches);
  } catch (ex) {
    el.innerHTML = `<div class="empty">${ex.message}</div>`;
  }
}

function renderMatches(el, matches) {
  if (!matches.length) {
    el.innerHTML = '<div class="empty">No matches yet.<br/>Go to My Resumes and click "Match Jobs" on a resume.</div>';
    return;
  }
  el.innerHTML = matches.map(m => matchItem(m)).join('');
}

function matchItem(m) {
  const lbl = label(m.finalScore);
  const cls = lbl.toLowerCase();
  return `
  <div class="match-item">
    <div>
      <div class="mi-title">${esc(m.jobTitle)}</div>
      <div class="mi-scores">
        <span class="mi-score">Skill Match: <strong>${(m.skillScore||0).toFixed(1)}%</strong></span>
        <span class="mi-score">Text Similarity: <strong>${(m.textSimilarityScore||0).toFixed(1)}%</strong></span>
        <span class="mi-score">Final Score: <strong>${(m.finalScore||0).toFixed(1)}%</strong></span>
      </div>
      <div class="score-bar">
        <div class="score-fill fill-${cls}" style="width:${m.finalScore||0}%"></div>
      </div>
    </div>
    <div class="badge badge-${cls}">${lbl}<br/><span style="font-size:1.1rem">${(m.finalScore||0).toFixed(0)}%</span></div>
  </div>`;
}

/* ════════════════════════════════════════
   RUN MATCH
════════════════════════════════════════ */
async function runMatch(resumeId, fileName) {
  document.getElementById('matchModalTitle').textContent = `Matching: ${fileName}`;
  document.getElementById('matchModalBody').innerHTML =
    '<div class="loading-wrap"><div class="spinner"></div><p>AI is analyzing your resume…</p></div>';
  openModal('matchModal');

  try {
    const result = await api('POST', `/matches/${resumeId}`);
    const matches = result?.matches || [];
    if (!matches.length) {
      document.getElementById('matchModalBody').innerHTML =
        '<div class="empty" style="border:none">No jobs to match against. Post some jobs first.</div>';
      return;
    }
    const el = document.getElementById('matchModalBody');
    el.innerHTML = '';
    matches.forEach(m => { el.innerHTML += matchItem(m); });
    // refresh KPIs
    loadDashboard();
  } catch (ex) {
    document.getElementById('matchModalBody').innerHTML =
      `<div class="form-err">${ex.message}</div>`;
  }
}

/* ════════════════════════════════════════
   UPLOAD RESUME
════════════════════════════════════════ */
function openUploadModal() {
  S.selectedFile = null;
  document.getElementById('filePreview').classList.add('hidden');
  document.getElementById('uploadErr').classList.add('hidden');
  document.getElementById('uploadBtn').disabled = true;
  document.getElementById('fileInput').value = '';
  openModal('uploadModal');
}

function onFileSelect(e) {
  const f = e.target.files[0];
  if (f) setFile(f);
}

function setFile(f) {
  S.selectedFile = f;
  const el = document.getElementById('filePreview');
  el.innerHTML = `📎 <strong>${esc(f.name)}</strong> <span style="color:var(--muted)">(${(f.size/1024).toFixed(0)} KB)</span>`;
  el.classList.remove('hidden');
  document.getElementById('uploadBtn').disabled = false;
}

async function doUpload() {
  if (!S.selectedFile) return;
  const btn = document.getElementById('uploadBtn');
  const err = document.getElementById('uploadErr');
  err.classList.add('hidden');
  setLoading(btn, 'Analyzing…');
  const form = new FormData();
  form.append('file', S.selectedFile);
  try {
    const r = await api('POST', '/resume/upload', form, true);
    closeModal('uploadModal');
    toast(`Resume uploaded! ${r.extractedSkills?.length || 0} skills extracted.`, 'success');
    loadDashboard();
    if (document.getElementById('view-resumes').classList.contains('active')) loadResumes();
  } catch (ex) {
    err.textContent = ex.message; err.classList.remove('hidden');
  } finally { resetBtn(btn, 'Analyze Resume'); btn.disabled = !S.selectedFile; }
}

// Drag & drop
document.addEventListener('DOMContentLoaded', () => {
  const zone = document.getElementById('dropZone');
  if (!zone) return;
  zone.addEventListener('dragover', e => { e.preventDefault(); zone.classList.add('over'); });
  zone.addEventListener('dragleave', () => zone.classList.remove('over'));
  zone.addEventListener('drop', e => {
    e.preventDefault(); zone.classList.remove('over');
    const f = e.dataTransfer.files[0];
    if (f) setFile(f);
  });
});

/* ════════════════════════════════════════
   CREATE JOB
════════════════════════════════════════ */
function openJobModal() {
  document.getElementById('jobTitle').value = '';
  document.getElementById('jobDesc').value  = '';
  document.getElementById('skillRows').innerHTML = '';
  document.getElementById('jobErr').classList.add('hidden');
  addSkillRow(); addSkillRow(); addSkillRow();
  openModal('jobModal');
}

function addSkillRow() {
  const row = document.createElement('div');
  row.className = 'skill-row';
  row.innerHTML = `
    <div class="field" style="flex:1;margin:0">
      <input type="text" placeholder="Skill name (e.g. Java, Spring Boot)"/>
    </div>
    <div class="field" style="width:140px;margin:0">
      <select>
        <option value="MANDATORY">★ Mandatory</option>
        <option value="OPTIONAL">○ Optional</option>
      </select>
    </div>
    <button class="rm" onclick="this.parentElement.remove()">✕</button>`;
  document.getElementById('skillRows').appendChild(row);
}

async function doCreateJob() {
  const err = document.getElementById('jobErr');
  err.classList.add('hidden');
  const title = document.getElementById('jobTitle').value.trim();
  const desc  = document.getElementById('jobDesc').value.trim();
  if (!title || !desc) {
    err.textContent = 'Title and description are required.';
    err.classList.remove('hidden'); return;
  }
  const skills = [];
  document.querySelectorAll('#skillRows .skill-row').forEach(row => {
    const name = row.querySelector('input').value.trim();
    const weight = row.querySelector('select').value;
    if (name) skills.push({ skillName: name, weight });
  });
  try {
    await api('POST', '/jobs', { title, description: desc, skills });
    closeModal('jobModal');
    toast('Job posted successfully!', 'success');
    loadJobs();
    loadDashboard();
  } catch (ex) {
    err.textContent = ex.message; err.classList.remove('hidden');
  }
}

/* ════════════════════════════════════════
   MODAL HELPERS
════════════════════════════════════════ */
function openModal(id)  { document.getElementById(id).classList.remove('hidden'); }
function closeModal(id) { document.getElementById(id).classList.add('hidden'); }
document.addEventListener('click', e => {
  if (e.target.classList.contains('modal-bg')) e.target.classList.add('hidden');
});

/* ════════════════════════════════════════
   TOAST
════════════════════════════════════════ */
let _tt;
function toast(msg, type = 'success') {
  const el = document.getElementById('toast');
  el.textContent = msg;
  el.className = `toast ${type}`;
  el.classList.remove('hidden');
  clearTimeout(_tt);
  _tt = setTimeout(() => el.classList.add('hidden'), 3500);
}

/* ════════════════════════════════════════
   UTILS
════════════════════════════════════════ */
function show(id) { document.getElementById(id).classList.remove('hidden'); }
function hide(id) { document.getElementById(id).classList.add('hidden'); }
function esc(s)   { return String(s||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;'); }
function fmtDate(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleDateString('en-US',{month:'short',day:'numeric',year:'numeric'});
}
function label(score) {
  if (score >= 80) return 'Excellent';
  if (score >= 60) return 'Good';
  if (score >= 40) return 'Fair';
  return 'Low';
}
function setLoading(btn, text) { btn.disabled = true; btn.textContent = text; }
function resetBtn(btn, text)   { btn.disabled = false; btn.textContent = text; }

/* ════════════════════════════════════════
   BOOT
════════════════════════════════════════ */
function isTokenExpired(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp * 1000 < Date.now();
  } catch { return true; }
}

if (S.token && !isTokenExpired(S.token)) {
  showApp();
} else {
  // Clear any stale token
  localStorage.removeItem('ra_token');
  localStorage.removeItem('ra_email');
  S.token = null; S.email = null;
  showLanding();
}
