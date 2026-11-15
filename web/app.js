/* IRHC web client. Demo data stays in this browser; configure Supabase for authenticated persistence. */
(() => {
'use strict';
const $ = id => document.getElementById(id);
const config = window.IRHC_CONFIG || {};
const hasBackend = Boolean(window.supabase && config.supabaseUrl && config.supabaseAnonKey);
const db = hasBackend ? window.supabase.createClient(config.supabaseUrl, config.supabaseAnonKey) : null;
const seedProviders = [
 {name:'Dr. Hana Bekele',specialty:'Reproductive health',facility:'Community Health Centre'},
 {name:'Nurse Selam Tesfaye',specialty:'Family planning',facility:'Women’s Wellness Clinic'},
 {name:'Dr. Dawit Mekonnen',specialty:'Maternal health',facility:'Primary Care Centre'}
];
const resources = [
 {category:'Family planning',title:'Understanding family planning options',summary:'An overview of family planning methods and how to discuss personal options with a qualified professional.',body:'Family planning includes a range of methods and services. The best option depends on individual health, preferences and circumstances. A qualified health professional can explain benefits, limitations, possible side effects and access.'},
 {category:'Maternal health',title:'Preparing for antenatal care visits',summary:'Questions to ask and information to bring to routine antenatal visits.',body:'Antenatal care supports the health of pregnant people and babies. Attend recommended visits and discuss medicines, symptoms, nutrition and any concerns with a qualified provider. Seek urgent care for severe or worrying symptoms.'},
 {category:'Sexual health',title:'STI prevention, testing and support',summary:'General information about prevention, testing and accessing appropriate care.',body:'Many sexually transmitted infections can be prevented or treated. Testing is the only way to know the status for many infections. A healthcare professional can explain confidential testing and treatment options.'},
 {category:'Adolescent health',title:'Adolescent health and trusted support',summary:'Age-appropriate guidance for young people, caregivers and educators.',body:'Young people benefit from accurate, age-appropriate information and access to respectful healthcare. Questions should be addressed without stigma. Confidentiality and consent requirements depend on applicable local law.'},
 {category:'Healthy relationships',title:'Respect, consent and healthy relationships',summary:'Information about respect, boundaries, safety and where to seek support.',body:'Healthy relationships are based on respect, communication, autonomy and freely given consent. Anyone who feels unsafe can seek support from a trusted person or qualified local service.'},
 {category:'Wellbeing',title:'Reproductive health and wellbeing',summary:'A broad introduction to reproductive health and routine preventive care.',body:'Reproductive health includes physical, emotional and social wellbeing. Routine preventive care and open conversations with qualified professionals can help people make informed decisions.'}
];
let user = null, profile = null, demoAppointments = JSON.parse(localStorage.getItem('irhc_appointments') || 'null') || [];
let demoMessages = JSON.parse(localStorage.getItem('irhc_messages') || '[]');
let appointments = [...demoAppointments], providers = [...seedProviders], authMode = 'login', currentThread = null;
const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const notify = message => { $('notice').textContent = message; $('notice').classList.remove('hidden'); window.clearTimeout(notify.timer); notify.timer = window.setTimeout(()=>$('notice').classList.add('hidden'),5000); };
const setConnection = (label,ok=false) => { $('connection').innerHTML = '<i class="'+(ok?'online':'')+'"></i> '+escapeHtml(label); };
const prettyDate = value => value ? new Date(value+'T00:00:00').toLocaleDateString(undefined,{year:'numeric',month:'short',day:'numeric'}) : '';
function showPage(name) {
 document.querySelectorAll('.page').forEach(el=>el.classList.toggle('hidden',el.id!==name));
 document.querySelectorAll('[data-page]').forEach(el=>el.classList.toggle('active',el.dataset.page===name));
 $('crumb').textContent = name.charAt(0).toUpperCase()+name.slice(1);
 if(name==='appointments') renderAppointments(); if(name==='reports') renderReports();
}
document.querySelectorAll('[data-page]').forEach(el=>el.addEventListener('click',()=>showPage(el.dataset.page)));
function renderAppointments() {
 $('count').textContent=appointments.length;
 $('rows').innerHTML=appointments.map(a=>'<tr><td><b>'+escapeHtml(a.reference_code||a.id||'DEMO')+'</b></td><td>'+escapeHtml(a.provider_name||a.provider||'Provider pending')+'</td><td>'+escapeHtml(prettyDate(a.appointment_date||a.date))+'<br>'+escapeHtml((a.appointment_time||a.time||'').slice(0,5))+'</td><td>'+escapeHtml((a.consultation_mode||a.mode||'').replace('_',' '))+'</td><td><span class="status '+escapeHtml(a.status||'pending')+'">'+escapeHtml(a.status||'pending')+'</span></td></tr>').join('');
 $('emptyAppointments').classList.toggle('hidden',appointments.length>0);
 const upcoming=[...appointments].filter(a=>(a.appointment_date||a.date)>=new Date().toISOString().slice(0,10)).sort((a,b)=>(a.appointment_date||a.date).localeCompare(b.appointment_date||b.date))[0];
 $('next').textContent=upcoming ? (upcoming.provider_name||upcoming.provider)+' · '+prettyDate(upcoming.appointment_date||upcoming.date)+' · '+(upcoming.appointment_time||upcoming.time||'')+' · '+(upcoming.status||'pending') : 'No upcoming appointment. Request one when you are ready.';
 $('messageCount').textContent=currentThread?'1':demoMessages.length?'1':'0';
}
function renderReports(){ $('reportAppts').textContent=appointments.length; $('reportPending').textContent=appointments.filter(a=>a.status==='pending').length; $('reportCompleted').textContent=appointments.filter(a=>a.status==='completed').length; }
function renderResources(query='') {
 const filtered=resources.filter(r=>(r.title+' '+r.category+' '+r.summary).toLowerCase().includes(query.toLowerCase()));
 $('articles').innerHTML=filtered.map((r,i)=>'<article class="resource-card"><span class="resource-icon">'+['♡','✚','⌁','◉','☼','✧'][resources.indexOf(r)]+'</span><span class="eyebrow">'+escapeHtml(r.category)+'</span><h3>'+escapeHtml(r.title)+'</h3><p>'+escapeHtml(r.summary)+'</p><button class="inline-link" data-resource="'+resources.indexOf(r)+'">Read overview →</button></article>').join('')||'<p class="empty">No resources match your search.</p>';
 document.querySelectorAll('[data-resource]').forEach(b=>b.addEventListener('click',()=>{const r=resources[Number(b.dataset.resource)];alert(r.title+'\n\n'+r.body+'\n\nEducational information only; consult a qualified professional for personal advice.')}));
 $('resourceCount').textContent=resources.length;
}
function renderProviders() {
 $('providerList').innerHTML=providers.map(p=>'<article class="provider-card"><span class="provider-avatar">✚</span><div><h3>'+escapeHtml(p.name)+'</h3><p>'+escapeHtml(p.specialty)+'</p><small>'+escapeHtml(p.facility||'Participating facility')+'</small></div><button class="outline-button" data-book-provider="'+escapeHtml(p.name)+'">Request visit</button></article>').join('');
 document.querySelectorAll('[data-book-provider]').forEach(b=>b.addEventListener('click',()=>{openBooking(b.dataset.bookProvider)}));
}
function renderNotifications(){
 $('notificationList').innerHTML=appointments.length?appointments.slice(0,8).map(a=>'<div class="notification-row"><span class="stat-icon">▣</span><div><b>Appointment '+escapeHtml(a.status||'requested')</b><p>'+escapeHtml(a.reference_code||a.id||'Request')+' · '+escapeHtml(prettyDate(a.appointment_date||a.date))+'</p></div></div>').join(''):'<div class="empty">You have no new notifications.</div>';
}
function renderMessages(){
 $('messages').innerHTML=demoMessages.map(m=>'<div class="chat-bubble '+(m.mine?'mine':'')+'"><p>'+escapeHtml(m.body)+'</p><small>'+escapeHtml(m.created_at||'Demo message')+'</small></div>').join('')||'<p class="empty">Sign in and request an appointment to start a consultation when the provider confirms it.</p>';
 $('messageCount').textContent=demoMessages.length?1:0;
}
function openBooking(providerName='') {
 if(providerName) $('provider').value=providerName;
 $('modal').showModal();
}
$('bookTop').onclick=$('bookBtn').onclick=()=>openBooking();
$('close').onclick=()=>$('modal').close();
$('date').min=new Date().toISOString().slice(0,10);
$('bookingForm').addEventListener('submit',async e=>{
 e.preventDefault();
 const chosen=$('provider').value, providerRecord=providers.find(p=>p.name===chosen);
 const payload={appointment_date:$('date').value,appointment_time:$('time').value,consultation_mode:$('mode').value,reason:$('reason').value.trim()||null,status:'pending'};
 try {
  if(db && user) {
   let providerId=null;
   const {data:prov}=await db.from('providers').select('id,profiles!inner(full_name)').eq('profiles.full_name',chosen).maybeSingle();
   providerId=prov?.id||null;
   const {data,error}=await db.from('appointments').insert({...payload,patient_id:user.id,provider_id:providerId}).select().single();
   if(error) throw error;
   appointments.unshift({...data,provider_name:chosen});
   if(providerId){const {data:thread,error:threadError}=await db.from('consultation_threads').insert({appointment_id:data.id,patient_id:user.id,provider_id:providerId}).select().single();if(!threadError&&thread)currentThread=thread.id;}
  } else {
   const item={...payload,id:'DEMO-'+Date.now(),reference_code:'IRH-'+Math.random().toString(36).slice(2,8).toUpperCase(),provider:chosen,provider_name:chosen,date:payload.appointment_date,time:payload.appointment_time};
   appointments.unshift(item); demoAppointments=appointments; localStorage.setItem('irhc_appointments',JSON.stringify(demoAppointments));
  }
  $('modal').close(); $('bookingForm').reset(); $('date').min=new Date().toISOString().slice(0,10); renderAppointments(); renderNotifications(); showPage('appointments'); notify('Appointment request saved. It is pending provider confirmation.');
 } catch(err){notify('Could not save the request: '+(err.message||'Please try again.'));}
});
$('messageForm').addEventListener('submit',async e=>{
 e.preventDefault(); const body=$('message').value.trim(); if(!body)return;
 if(db&&user&&currentThread){try{const {error}=await db.from('consultation_messages').insert({thread_id:currentThread,sender_id:user.id,body});if(error)throw error;}catch(err){notify('Message was not sent: '+err.message);return;}}
 else {demoMessages.push({body,mine:true,created_at:new Date().toLocaleString()});localStorage.setItem('irhc_messages',JSON.stringify(demoMessages));}
 $('message').value=''; renderMessages(); notify('Message added to the current demo conversation.'); 
});
$('search').addEventListener('input',e=>renderResources(e.target.value));
$('exportCsv').addEventListener('click',()=>{
 const cols=['reference_code','provider_name','appointment_date','appointment_time','consultation_mode','status'];
 const lines=[cols.join(','),...appointments.map(a=>cols.map(k=>'"'+String(a[k]??a[k.replace('_name','')]??'').replace(/"/g,'""')+'"').join(','))];
 const url=URL.createObjectURL(new Blob([lines.join('\n')],{type:'text/csv'}));const a=document.createElement('a');a.href=url;a.download='irhc-my-appointments.csv';a.click();URL.revokeObjectURL(url);
});
function setAuthUI(){
 const name=profile?.full_name||user?.email||'Guest user'; $('sideName').textContent=name; $('sideRole').textContent=profile?.role|| (user?'Community member':'Demo mode');
 $('authOpen').textContent=user?'Sign out':'Sign in'; $('headerAuth').textContent=user?'Sign out':'Sign in / Register';
 document.querySelectorAll('[data-staff]').forEach(el=>el.classList.toggle('hidden',!profile||!['healthcare_professional','manager','administrator'].includes(profile.role)));
}
async function loadBackendData(){
 if(!db||!user)return;
 const {data:profileData}=await db.from('profiles').select('*').eq('id',user.id).maybeSingle(); profile=profileData||null;
 const {data:appts,error}=await db.from('appointments').select('*,providers(professional_title,specialty,profiles(full_name))').order('appointment_date',{ascending:true});
 if(!error&&appts)appointments=appts.map(a=>({...a,provider_name:a.providers?.profiles?.full_name||a.providers?.professional_title||'Care team'}));
 const {data:providerData}=await db.from('providers').select('id,professional_title,specialty,facility_name,profiles(full_name)').eq('is_active',true);
 if(providerData?.length)providers=providerData.map(p=>({id:p.id,name:p.profiles?.full_name||p.professional_title,specialty:p.specialty,facility:p.facility_name}));
 const {data:threads}=await db.from('consultation_threads').select('id,created_at').eq('patient_id',user.id).order('created_at',{ascending:false}).limit(1);
 if(threads?.length){currentThread=threads[0].id;const {data:msgs}=await db.from('consultation_messages').select('body,created_at,sender_id').eq('thread_id',currentThread).order('created_at');if(msgs)demoMessages=msgs.map(m=>({body:m.body,mine:m.sender_id===user.id,created_at:new Date(m.created_at).toLocaleString()}));renderMessages();}
 renderAppointments();renderProviders();renderNotifications();setAuthUI();
}
function openAuth(){if(user){db?.auth.signOut().then(()=>{user=null;profile=null;appointments=demoAppointments;setAuthUI();renderAppointments();setConnection('Demo mode');notify('You have signed out.');});return;}authMode='login';$('authTitle').textContent='Sign in to IRHC';$('authSubmit').textContent='Sign in';$('signupNameWrap').classList.add('hidden');$('signupName').required=false;$('authSwitchText').innerHTML='New to IRHC? <button type="button" id="authSwitch" class="inline-link">Create an account</button>';bindAuthSwitch();$('authModal').showModal();}
function bindAuthSwitch(){const b=$('authSwitch');if(b)b.onclick=()=>{authMode=authMode==='login'?'signup':'login';$('authTitle').textContent=authMode==='login'?'Sign in to IRHC':'Create your IRHC account';$('authSubmit').textContent=authMode==='login'?'Sign in':'Create account';$('signupNameWrap').classList.toggle('hidden',authMode!=='signup');$('signupName').required=authMode==='signup';$('authSwitchText').innerHTML=authMode==='login'?'New to IRHC? <button type="button" id="authSwitch" class="inline-link">Create an account</button>':'Already registered? <button type="button" id="authSwitch" class="inline-link">Sign in</button>';bindAuthSwitch();};}
$('authOpen').onclick=openAuth;$('headerAuth').onclick=openAuth;$('authClose').onclick=()=>$('authModal').close();bindAuthSwitch();
$('authForm').addEventListener('submit',async e=>{
 e.preventDefault();if(!db){notify('Supabase is not configured. Demo mode remains available; see web/README.md to connect a project.');return;}
 const email=$('email').value.trim(),password=$('password').value;
 try{
  if(authMode==='signup'){
   const {data,error}=await db.auth.signUp({email,password,options:{data:{full_name:$('signupName').value.trim()}}});if(error)throw error;
   notify('Account created. Check your email if confirmation is enabled. Your community profile is created automatically by the database.');
  } else {const {error}=await db.auth.signInWithPassword({email,password});if(error)throw error;notify('Signed in successfully.');}
  $('authModal').close();
 }catch(err){notify(err.message||'Authentication failed.');}
});
async function init(){
 $('today').textContent=new Date().toLocaleDateString(undefined,{weekday:'long',month:'long',day:'numeric',year:'numeric'});renderAppointments();renderResources();renderProviders();renderNotifications();renderMessages();setAuthUI();
 if(!hasBackend){setConnection('Demo mode');return;}
 setConnection('Connecting…');
 const {data:{session}}=await db.auth.getSession();user=session?.user||null;
 if(user){await loadBackendData();setConnection('Connected',true);}else setConnection('Sign in required');
 db.auth.onAuthStateChange(async(_event,session)=>{user=session?.user||null;if(user){await loadBackendData();setConnection('Connected',true);}else{profile=null;appointments=demoAppointments;setAuthUI();renderAppointments();setConnection('Sign in required');}});
}
init().catch(err=>{console.error(err);setConnection('Backend unavailable');notify('Backend connection failed. Demo mode remains available.');});
})();
