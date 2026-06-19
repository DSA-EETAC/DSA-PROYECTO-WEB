const URL_BASE = "https://dsa2.upc.edu/api/juego";

// --- SISTEMA DE NOTIFICACIONES (TOAST) ---
let timeoutMensaje;
function avisar(texto, esError) {
    const divMensaje = document.getElementById('mensaje-sistema');
    divMensaje.innerText = texto;
    divMensaje.style.borderLeftColor = esError ? "#ff4d4d" : "#ffcc33";
    divMensaje.classList.remove('oculto');

    // Auto-ocultar a los 4 segundos
    clearTimeout(timeoutMensaje);
    timeoutMensaje = setTimeout(() => {
        divMensaje.classList.add('oculto');
    }, 4000);
}

// --- NAVEGACIÓN ---
function cambiarVista(vista) {
    document.getElementById('seccion-login').classList.toggle('hidden', vista === 'registro');
    document.getElementById('seccion-registro').classList.toggle('hidden', vista === 'login');
}

function cambiarPestana(idPestana, idBoton) {
    document.querySelectorAll('.tab-content').forEach(tab => tab.style.display = 'none');
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));

    document.getElementById(idPestana).style.display = 'block';
    document.getElementById(idBoton).classList.add('active');

    if (idPestana === 'tab-grupos') cargarGrupos();
    if (idPestana === 'tab-eventos') cargarEventos();
}

function cerrarSesion() {
    localStorage.removeItem("jugadorActual");
    document.getElementById("seccion-dashboard").classList.add("hidden");
    document.getElementById("contenedor-login").classList.remove("hidden");
    document.getElementById("seccion-login").classList.remove("hidden");
    document.getElementById("seccion-registro").classList.add("hidden");
    document.getElementById("login-nombre").value = "";
    document.getElementById("login-pass").value = "";
}

// --- LOGIN Y REGISTRO ---
async function hacerRegistro() {
    const nombre = document.getElementById('reg-nombre').value;
    const password = document.getElementById('reg-pass').value;
    const correo = document.getElementById('reg-correo').value;
    const confirmarPassword = document.getElementById('reg-pass-confirm').value.trim();

    if (!nombre || !password || !confirmarPassword|| !correo) return avisar("Rellena todos los campos.", true);
    if (password !== confirmarPassword) return avisar("Las contraseñas no coinciden.", true);
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo)) return avisar("Correo no válido.", true);

    try {
        const respuesta = await fetch(`${URL_BASE}/registro`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept' : 'application/json' },
            body: JSON.stringify({ nombre, password, mail: correo })
        });

        if (respuesta.status === 201) {
            avisar("¡Explorador registrado con éxito!", false);
            const datosUsuario = await respuesta.json();
            iniciarDashboard(datosUsuario);
        } else if (respuesta.status === 400) {
            avisar(await respuesta.text(), true);
        } else if (respuesta.status === 409) {
            avisar("Nombre ya en uso.", true);
        }
    } catch (error) { avisar("Servidor desconectado.", true); }
}

async function hacerLogin() {
    const nombre = document.getElementById('login-nombre').value;
    const password = document.getElementById('login-pass').value;

    if (!nombre || !password) return avisar("Rellena tus credenciales.", true);

    try {
        const respuesta = await fetch(`${URL_BASE}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({ nombre, password })
        });

        if (respuesta.status === 200) {
            avisar("¡Acceso concedido!", false);
            const datosUsuario = await respuesta.json();
            iniciarDashboard(datosUsuario);
        } else if (respuesta.status === 401) {
            avisar("Credenciales incorrectas.", true);
        } else { avisar(await respuesta.text(), true); }
    } catch (error) { avisar("Servidor desconectado.", true); }
}

async function iniciarDashboard(datos) {
    localStorage.setItem("jugadorActual", datos.nombre);
    document.getElementById("perfil-nombre").innerText = datos.nombre.toUpperCase();
    document.getElementById("contador-monedas").innerText = datos.monedas;

    await actualizarMochilaDesdeBaseDeDatos(datos.nombre);

    document.getElementById("contenedor-login").classList.add("hidden");
    document.getElementById("seccion-dashboard").classList.remove("hidden");
    cambiarPestana('tab-perfil', 'btn-perfil');
}

// --- MOCHILA Y TIENDA ---
async function actualizarMochilaDesdeBaseDeDatos(nombreUsuario) {
    if (!nombreUsuario) return;
    try {
        const respuesta = await fetch(`${URL_BASE}/inventario/${nombreUsuario}`, { method: 'GET', headers: { 'Accept': 'application/json' }});
        if (respuesta.status === 200) {
            const inventario = await respuesta.json();
            cargarMochilaDesdeJava(inventario.objetos);
        }
    } catch (e) { console.error("Error inventario", e); }
}

function cargarMochilaDesdeJava(lista) {
    const caja = document.getElementById("contenedor-inventario");
    if (!lista || lista.length === 0) {
        caja.innerHTML = `<p class="texto-vacio">Tu mochila está vacía por ahora.</p>`;
        return;
    }
    let html = `<ul id="lista-mochila" style="list-style:none; padding:0; font-size:1.2rem;">`;
    lista.forEach(obj => { html += `<li style="background:var(--bg-panel); padding:15px; margin-bottom:10px; border-radius:6px; border:1px solid #333;">🎒 ${obj}</li>`; });
    caja.innerHTML = html + `</ul>`;
}

async function comprarItem(nombreObjeto, precio) {
    const nombreJugador = localStorage.getItem("jugadorActual");
    if (!nombreJugador) return avisar("Inicia sesión primero.", true);

    try {
        const respuesta = await fetch(`${URL_BASE}/comprar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({ nombreJugador, nombreObjeto, precio })
        });

        if (respuesta.status === 200) {
            avisar(`¡Comprado: ${nombreObjeto}!`, false);
            const cont = document.getElementById("contador-monedas");
            cont.innerText = parseInt(cont.innerText) - precio;
            actualizarMochilaDesdeBaseDeDatos(nombreJugador); // Refresca inventario real
        } else if (respuesta.status === 402) {
            avisar("Monedas insuficientes.", true);
        }
    } catch (e) { avisar("Error de tienda.", true); }
}

function cargarTienda() {
    fetch(`${URL_BASE}/tienda`)
        .then(res => res.json())
        .then(data => dibujarTienda(data.items))
        .catch(() => document.getElementById('contenedor-items-tienda').innerHTML = '<p class="texto-vacio">Mercader no disponible.</p>');
}

function dibujarTienda(listaItems) {
    const cont = document.getElementById('contenedor-items-tienda');
    cont.innerHTML = '';
    if (!listaItems || listaItems.length === 0) return cont.innerHTML = '<p class="texto-vacio">Sin existencias.</p>';

    listaItems.forEach(item => {
        cont.innerHTML += `
            <div class="tarjeta-grid" style="text-align: center;">
                <h4>${item.nombre}</h4>
                <p style="color:var(--gold); font-size:1.5rem; font-weight:bold;">${item.precio} 🪙</p>
                <p>${item.tipo}</p>
                <button class="btn-primary" onclick="comprarItem('${item.nombre}', ${item.precio})">COMPRAR</button>
            </div>`;
    });
}
window.onload = cargarTienda;

// --- GRUPOS ---
async function cargarGrupos() {
    try {
        const res = await fetch(`${URL_BASE}/grupos`, { method: 'GET', headers: { 'Accept': 'application/json' }});
        if (res.status === 200) { const data = await res.json(); dibujarGrupos(data.grupos); }
    } catch (e) { document.getElementById('contenedor-grupos').innerHTML = '<p class="texto-vacio">Error al cargar grupos.</p>'; }
}

function dibujarGrupos(listaGrupos) {
    const cont = document.getElementById('contenedor-grupos');
    cont.innerHTML = '';
    if (!listaGrupos || listaGrupos.length === 0) return cont.innerHTML = '<p class="texto-vacio">No hay grupos.</p>';

    listaGrupos.forEach(g => {
        cont.innerHTML += `
            <div class="tarjeta-grid">
                <div>
                    <h4>🛡️ ${g.nombre}</h4>
                    <p>Identificador de Gremio: <strong>#${g.id}</strong></p>
                </div>
                <button class="btn-primary" onclick="unirseGrupo('${g.id}', '${g.nombre}')">UNIRSE AL GREMIO</button>
            </div>`;
    });
}

async function unirseGrupo(idGrupo, nombreGrupo) {
    const nombre = localStorage.getItem("jugadorActual");
    try {
        const res = await fetch(`${URL_BASE}/grupos/${idGrupo}/unirse`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ nombre })
        });
        if (res.status === 200) avisar(`Solicitud enviada a ${nombreGrupo}.`, false);
        else avisar("Error al unirse.", true);
    } catch (e) { avisar("Servidor no responde.", true); }
}

async function consultarMiGrupo() {
    const nombre = localStorage.getItem("jugadorActual");
    try {
        const res = await fetch(`${URL_BASE}/usuarios/${nombre}/grupo`, { method: 'GET', headers: { 'Accept': 'application/json' }});
        if (res.status === 200) {
            const data = await res.json();
            const cont = document.getElementById("contenedor-grupo");
            document.getElementById("grupo-titulo").innerText = data.tieneGrupo ? `Mi Equipo: ${data.nombreGrupo}` : "Sin Grupo";
            document.getElementById("grupo-mensaje").innerText = data.tieneGrupo ? "Integrantes:" : "Busca un gremio en la pestaña Grupos.";

            const lista = document.getElementById("grupo-lista-miembros");
            lista.innerHTML = "";
            if(data.tieneGrupo){
                data.miembros.forEach(m => {
                    lista.innerHTML += `<li ${m === nombre ? 'style="color:var(--gold); font-weight:bold;"' : ''}>⚔️ ${m} ${m === nombre ? '(Tú)' : ''}</li>`;
                });
            }
            cont.classList.remove("hidden");
        }
    } catch (e) { avisar("Error consultando grupo.", true); }
}

// --- EVENTOS Y RANKING ---
async function cargarEventos() {
    const cont = document.getElementById('contenedor-eventos');
    cont.innerHTML = '<p class="texto-vacio">Buscando pergaminos...</p>';
    try {
        const res = await fetch(`${URL_BASE}/eventos`);
        if (res.status === 200) {
            const eventos = await res.json();
            cont.innerHTML = '';
            if (!eventos || eventos.length === 0) return cont.innerHTML = '<p class="texto-vacio">No hay misiones disponibles.</p>';

            eventos.forEach(ev => {
                const img = (ev.imagen_URL && ev.imagen_URL.trim() !== "") ? ev.imagen_URL : "img/sin-imagen.jpg";
                cont.innerHTML += `
                    <div class="tarjeta-grid">
                        <div>
                            <img src="${img}" class="tarjeta-img" onerror="this.src='img/sin-imagen.jpg';">
                            <h4>${ev.nombre}</h4>
                            <p>${ev.descripcion}</p>
                            <p style="color:var(--gold-dim); font-size:0.9rem;">⏳ ${ev.fecha_inicio.split(' ')[0]} - ${ev.fecha_fin.split(' ')[0]}</p>
                        </div>
                        <button class="btn-primary" onclick="inscribirseEvento('${ev.id}')">INSCRIBIRSE Y VER RANKING</button>
                    </div>`;
            });
        }
    } catch (e) { cont.innerHTML = '<p class="texto-vacio">Servidor de misiones caído.</p>'; }
}

async function inscribirseEvento(idEvento) {
    const username = localStorage.getItem("jugadorActual");
    try {
        const res = await fetch(`${URL_BASE}/eventos/inscripcion`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ username, idEvento })
        });
        if (res.status === 201 || res.status === 200) avisar('¡Inscrito correctamente!', false);
        cargarRankingEvento(idEvento);
    } catch (e) { avisar("Error al inscribirse.", true); }
}

async function cargarRankingEvento(idEvento) {
    const cont = document.getElementById('contenedor-eventos');
    cont.innerHTML = '<p class="texto-vacio">Cargando clasificación...</p>';
    try {
        const res = await fetch(`${URL_BASE}/eventos/${idEvento}/ranking`);
        if (res.status === 200) dibujarRanking(await res.json());
        else cont.innerHTML = '<p class="texto-vacio">Error de clasificación.</p>';
    } catch (e) { cont.innerHTML = '<p class="texto-vacio">Error de conexión.</p>'; }
}

function dibujarRanking(lista) {
    const cont = document.getElementById('contenedor-eventos');
    const yo = localStorage.getItem("jugadorActual");

    let html = `
        <div class="ranking-caja" style="grid-column: 1 / -1;">
            <h2 style="text-align:center; color:var(--gold); font-family:'Cinzel';">🏆 CLASIFICACIÓN 🏆</h2>
            <ul class="ranking-lista">
    `;

    if (lista.length === 0) return cont.innerHTML = html + `<p class="texto-vacio">Nadie ha puntuado. ¡Sé el primero!</p></ul></div>`;

    const top5 = lista.slice(0, 5);
    let estoyTop5 = false;

    top5.forEach((j, i) => {
        let claseColor = "";
        let medalla = "🏅";
        if (i===0) { claseColor = "r-oro"; medalla = "🥇"; }
        else if (i===1) { claseColor = "r-plata"; medalla = "🥈"; }
        else if (i===2) { claseColor = "r-bronce"; medalla = "🥉"; }

        let nombreTx = j.nombreJugador;
        if (j.nombreJugador === yo) { estoyTop5 = true; nombreTx = `<span style="color:var(--success)">${yo} (Tú)</span>`; }

        html += `<li class="ranking-item ${claseColor}"><span>${medalla} <strong>${nombreTx}</strong></span> <span style="color:#ccc">${j.puntuacion} pts</span></li>`;
    });

    if (!estoyTop5) {
        const miIndice = lista.findIndex(j => j.nombreJugador === yo);
        if (miIndice !== -1) {
            html += `
                <li style="text-align:center; padding: 10px; color:#666;">•••</li>
                <li class="ranking-item r-tu"><span>${miIndice + 1}º <strong><span style="color:var(--success)">${yo} (Tú)</span></strong></span> <span style="color:#ccc">${lista[miIndice].puntuacion} pts</span></li>
            `;
        }
    }

    html += `</ul><button class="btn-primary" style="margin-top:25px;" onclick="cargarEventos()">⬅ VOLVER A MISIONES</button></div>`;
    cont.innerHTML = html;
}