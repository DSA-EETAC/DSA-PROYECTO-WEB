const URL_BASE = "https://dsa2.upc.edu/api/juego";

async function hacerRegistro() {
    // Datos del HTML
    const nombre = document.getElementById('reg-nombre').value;
    const password = document.getElementById('reg-pass').value;
    const correo = document.getElementById('reg-correo').value;
    const confirmarPassword = document.getElementById('reg-pass-confirm').value.trim();

    // No enviar campos vacíos
    if (!nombre || !password || !confirmarPassword|| !correo) {
        avisar("¡Nombre, contraseña, confirmación y correo son obligatorios!", true);
        return;
    }

    if (password !== confirmarPassword) {
        avisar("Las contraseñas no coinciden. Revisa que las hayas escrito igual.", true);
        return;
    }

    const regexCorreo = /^[^\s@]+@[^\s@]+\.[^\s@]+$/; // Comprobacion de formato de correo
    if (!regexCorreo.test(correo)) {
        avisar("El formato del correo electrónico no es válido.", true);
        return;
    }

    // Creamos objeto formato JSON
    const datosUsuario = {
        nombre: nombre,
        password: password,
        mail: correo
    };

    try {
        // Enviamos petición POST a backend
        const respuesta = await fetch(`${URL_BASE}/registro`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept' : 'application/json' },
            body: JSON.stringify(datosUsuario)
        });

        if (respuesta.status === 201) {
            avisar("¡Explorador registrado con éxito!", false);
            const datosUsuario = await respuesta.json();

            localStorage.setItem("jugadorActual", nombre);
            document.getElementById("mensaje-bienvenida").innerText = "CAMPAMENTO DE " + datosUsuario.nombre.toUpperCase();
            document.getElementById("perfil-nombre").innerText = datosUsuario.nombre;
            document.getElementById("contador-monedas").innerText = datosUsuario.monedas;

            await actualizarMochilaDesdeBaseDeDatos(datosUsuario.nombre);

            // Limpiar campos de register
            document.getElementById('reg-nombre').value = "";
            document.getElementById('reg-pass').value = "";
            document.getElementById('reg-pass-confirm').value = "";
            document.getElementById('reg-correo').value = "";

            // Ocultamos la caja del login entera y mostramos el dashboard ancho
            document.getElementById("contenedor-login").classList.add("hidden");
            document.getElementById("seccion-dashboard").classList.remove("hidden");

            // Restablecemos internamente la vista a 'login' por si cierra sesión después
            document.getElementById("seccion-registro").classList.add("hidden");
            document.getElementById("seccion-login").classList.remove("hidden");

            cambiarPestana('tab-perfil', 'btn-perfil');

        } else if (respuesta.status === 400) {
            const mensajeError = await respuesta.text();
            avisar(mensajeError, true);

        } else if (respuesta.status === 409) {
            avisar("Ese nombre ya ha sido reclamado en este templo.", true);
        } else {
            avisar("Error desconocido en el servidor.", true);
        }
    } catch (error) {
        avisar("No hay conexión con el juego (Server Java apagado).", true);
        console.error("Error en fetch:", error);
    }
}

async function hacerLogin() {
    const nombre = document.getElementById('login-nombre').value;
    const password = document.getElementById('login-pass').value;

    if (!nombre || !password) {
        avisar("Rellena tus credenciales, explorador.", true);
        return;
    }

    const credenciales = {
        nombre: nombre,
        password: password
    };

    try {
        const respuesta = await fetch(`${URL_BASE}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify(credenciales)
        });

        if (respuesta.status === 200) {
            avisar("¡Acceso concedido! Abriendo el campamento...", false);
            const datosUsuario = await respuesta.json();
            console.log("Datos cargados desde Java:", datosUsuario);

            localStorage.setItem("jugadorActual", datosUsuario.nombre);

            document.getElementById("mensaje-bienvenida").innerText = "CAMPAMENTO DE " + datosUsuario.nombre.toUpperCase();
            document.getElementById("perfil-nombre").innerText = datosUsuario.nombre;
            document.getElementById("contador-monedas").innerText = datosUsuario.monedas;

            await actualizarMochilaDesdeBaseDeDatos(datosUsuario.nombre);

            // Ocultamos la caja del login entera y mostramos el dashboard ancho
            document.getElementById("contenedor-login").classList.add("hidden");
            document.getElementById("seccion-dashboard").classList.remove("hidden");

            cambiarPestana('tab-perfil', 'btn-perfil');

        } else if (respuesta.status === 400) {
            const mensajeError = await respuesta.text();
            avisar(mensajeError, true);
        } else if (respuesta.status === 401) {
            avisar("Nombre o contraseña incorrectos.", true);
        } else {
            avisar("Error en el sistema de acceso.", true);
        }
    } catch (error) {
        avisar("El servidor no responde.", true);
        console.error("Error en fetch:", error);
    }
}

async function actualizarMochilaDesdeBaseDeDatos(nombreUsuario) {
    if (!nombreUsuario || nombreUsuario === "undefined") {
        console.warn("Mochila: No hay usuario logueado, abortando carga.");
        return;
    }

    console.log("DEPURACIÓN MOCHILA: Intentando acceder a ->", `${URL_BASE}/inventario/${nombreUsuario}`);

    try {
        const respuesta = await fetch(`${URL_BASE}/inventario/${nombreUsuario}`, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });

        if (respuesta.status === 200) {
            const inventarioJugador = await respuesta.json();
            const listaItems = inventarioJugador.objetos;
            cargarMochilaDesdeJava(listaItems);
        }
    } catch (error) {
        console.error("Error al recuperar el inventario relacional:", error);
    }
}

function cambiarVista(vista) {
    const login = document.getElementById('seccion-login');
    const registro = document.getElementById('seccion-registro');
    document.getElementById('mensaje-sistema').innerText = "";

    if (vista === 'registro') {
        login.classList.add('hidden');
        registro.classList.remove('hidden');
    } else {
        registro.classList.add('hidden');
        login.classList.remove('hidden');
    }
}

function avisar(texto, esError) {
    const divMensaje = document.getElementById('mensaje-sistema');
    divMensaje.innerText = texto;
    divMensaje.style.color = esError ? "#ff4444" : "#ffcc33";
}

async function comprarItem(nombreObjeto, precio) {
    const nombreJugador = localStorage.getItem("jugadorActual");

    if (!nombreJugador || nombreJugador === "undefined" || nombreJugador === "null") {
        console.error("Error: No hay un usuario logueado en localStorage.");
        avisar("Debes iniciar sesión para comprar.", true);
        return;
    }

    const datosCompra = {
        nombreJugador: nombreJugador,
        nombreObjeto: nombreObjeto,
        precio: precio
    };

    console.log("Enviando compra a:", `${URL_BASE}/comprar`, "Datos:", datosCompra);

    try {
        const respuesta = await fetch(`${URL_BASE}/comprar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify(datosCompra)
        });

        if (respuesta.status === 200) {
            avisar("¡Has comprado " + nombreObjeto + "!", false);

            const contador = document.getElementById("contador-monedas");
            if (contador) {
                let monedasActuales = parseInt(contador.innerText) || 0;
                contador.innerText = monedasActuales - precio;
            }

            if (typeof actualizarMochilaHTML === 'function') {
                actualizarMochilaHTML(nombreObjeto);
            }

        } else if (respuesta.status === 402) {
            avisar("No tienes suficientes monedas para " + nombreObjeto + ".", true);
        } else {
            avisar("Error en la transacción. Código: " + respuesta.status, true);
        }
    } catch (error) {
        console.error("Error en el fetch:", error);
        avisar("El servidor de la tienda está caído o no responde.", true);
    }
}

function actualizarMochilaHTML(nuevoObjeto) {
    const cajaInventario = document.getElementById("tab-inventario");

    if (cajaInventario.innerHTML.includes("Tu mochila está vacía por ahora.")) {
        cajaInventario.innerHTML = `<h3 style="color: white; text-align: center;">Tus Pertinencias</h3><ul id="lista-mochila" style="color: gold; font-size: 1.1em;"></ul>`;
    }

    const lista = document.getElementById("lista-mochila");
    lista.innerHTML += `<li>✨ ${nuevoObjeto}</li>`;
}

function cambiarPestana(idPestana, idBoton) {
    document.getElementById('tab-tienda').style.display = 'none';
    document.getElementById('tab-inventario').style.display = 'none';
    document.getElementById('tab-perfil').style.display = 'none';
    document.getElementById('tab-grupos').style.display = 'none';
    document.getElementById('tab-eventos').style.display = 'none';

    document.getElementById('btn-tienda').classList.remove('active');
    document.getElementById('btn-inventario').classList.remove('active');
    document.getElementById('btn-perfil').classList.remove('active');
    document.getElementById('btn-grupos').classList.remove('active');
    document.getElementById('btn-eventos').classList.remove('active');

    document.getElementById(idPestana).style.display = 'block';
    document.getElementById(idBoton).classList.add('active');

    document.getElementById('mensaje-sistema').innerText = "";

    if (idPestana === 'tab-grupos') {
        cargarGrupos();
    }
    if (idPestana === 'tab-eventos') {
        cargarEventos();
    }
}

function cerrarSesion() {
    localStorage.removeItem("jugadorActual");

    document.getElementById("seccion-dashboard").classList.add("hidden");
    document.getElementById("contenedor-login").classList.remove("hidden");
    document.getElementById("seccion-login").classList.remove("hidden");
    document.getElementById("seccion-registro").classList.add("hidden");

    document.getElementById("login-nombre").value = "";
    document.getElementById("login-pass").value = "";

    document.getElementById('mensaje-sistema').innerText = "";
}

function cargarMochilaDesdeJava(listaInventario) {
    const cajaInventario = document.getElementById("tab-inventario");
    if (!listaInventario || listaInventario.length === 0) {
        cajaInventario.innerHTML = `
            <h3 style="color: white; text-align: center;">Tus Pertinencias</h3>
            <p style="color: #aaa; text-align: center;">Tu mochila está vacía por ahora.</p>`;
        return;
    }
    let htmlLista = `
        <h3 style="color: white; text-align: center;">Tus Pertinencias</h3>
        <ul id="lista-mochila" style="color: gold; font-size: 1.1em; list-style-type: none; padding: 0; text-align: center;">`;

    listaInventario.forEach(objeto => {
        htmlLista += `<li style="margin-bottom: 10px; background: rgba(0,0,0,0.5); padding: 5px; border-radius: 5px;">✨ ${objeto}</li>`;
    });
    htmlLista += `</ul>`;
    cajaInventario.innerHTML = htmlLista;
}

function cargarTienda() {
    fetch(`${URL_BASE}/tienda`)
        .then(response => {
            if (!response.ok) throw new Error("Error en la respuesta del servidor");
            return response.json();
        })
        .then(datosTienda => {
            const listaItems = datosTienda.items;
            dibujarTienda(listaItems);
        })
        .catch(error => {
            console.error('Error al cargar la tienda:', error);
            const contenedor = document.getElementById('contenedor-items-tienda');
            contenedor.innerHTML = '<p style="color: #ff4444; text-align: center;">Error de conexión con el mercader.</p>';
        });
}

function dibujarTienda(listaItems) {
    const contenedor = document.getElementById('contenedor-items-tienda');
    contenedor.innerHTML = '';

    if (!listaItems || listaItems.length === 0) {
        contenedor.innerHTML = '<p style="color: #aaa; text-align: center;">El mercader no tiene existencias hoy.</p>';
        return;
    }

    listaItems.forEach(item => {
        // Estilos limpios adaptados a CSS Grid
        const tarjetaHtml = `
            <div style="border: 1px solid rgba(255,255,255,0.2); padding: 15px; text-align: center; background: rgba(0,0,0,0.5); border-radius: 8px; display: flex; flex-direction: column; justify-content: space-between;">
                <h4 style="color: white; margin: 0 0 5px 0; font-family: 'Cinzel', serif;">${item.nombre}</h4>
                <p style="color: gold; font-weight: bold; margin-bottom: 5px;">${item.precio} 🪙</p>
                <p style="color: #ccc; font-size: 0.8em; margin-bottom: 12px;">${item.tipo}</p>
                <button class="btn-primary" style="padding: 8px; font-size: 0.9em;" onclick="comprarItem('${item.nombre}', ${item.precio})">COMPRAR</button>
            </div>
        `;
        contenedor.innerHTML += tarjetaHtml;
    });
}

window.onload = function() {
    cargarTienda();
};

async function cargarGrupos() {
    try {
        const respuesta = await fetch(`${URL_BASE}/grupos`, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });

        if (respuesta.status === 200) {
            const datosJson = await respuesta.json();
            const listaGrupos = datosJson.grupos;
            dibujarGrupos(listaGrupos);
        }
    } catch (error) {
        console.error("Error al cargar grupos:", error);
        document.getElementById('contenedor-grupos').innerHTML =
            '<p style="color: #ff4444; text-align: center;">Error de conexión con el gremio.</p>';
    }
}

function dibujarGrupos(listaGrupos) {
    const contenedor = document.getElementById('contenedor-grupos');
    contenedor.innerHTML = '';

    if (!listaGrupos || listaGrupos.length === 0) {
        contenedor.innerHTML = '<p style="color: #aaa; text-align: center;">No hay grupos disponibles ahora mismo.</p>';
        return;
    }

    listaGrupos.forEach(grupo => {
        // Estilos limpios adaptados a CSS Grid
        const tarjetaHtml = `
            <div style="border: 1px solid rgba(255,255,255,0.2); padding: 15px; text-align: center; background: rgba(0,0,0,0.5); border-radius: 8px; display: flex; flex-direction: column; justify-content: space-between;">
                <h4 style="color: white; margin: 0 0 5px 0; font-family: 'Cinzel', serif;">${grupo.nombre}</h4>
                <p style="color: #ccc; font-size: 0.8em; margin-bottom: 12px;">ID: ${grupo.id}</p>
                <button class="btn-primary" style="padding: 8px; font-size: 0.9em; background-color: #27ae60; border-color: #2ecc71;"
                onclick="unirseGrupo('${grupo.id}', '${grupo.nombre}')">UNIRSE</button>
            </div>
        `;
        contenedor.innerHTML += tarjetaHtml;
    });
}

async function unirseGrupo(idGrupo, nombreGrupo) {
    const nombreJugador = localStorage.getItem("jugadorActual");
    const datosUsuario = { nombre: nombreJugador };

    try {
        const respuesta = await fetch(`${URL_BASE}/grupos/${idGrupo}/unirse`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify(datosUsuario)
        });

        if (respuesta.status === 200) {
            avisar(`¡Éxito! Has solicitado unirte a ${nombreGrupo}.`, false);
        } else {
            avisar(`Error al intentar unirte a ${nombreGrupo}.`, true);
        }
    } catch (error) {
        avisar("El servidor de clanes no responde.", true);
    }
}

async function consultarMiGrupo() {
    const nombreJugador = localStorage.getItem("jugadorActual");

    if (!nombreJugador) {
        avisar("No se ha detectado ninguna sesión activa.", true);
        return;
    }

    try {
        const respuesta = await fetch(`${URL_BASE}/usuarios/${nombreJugador}/grupo`, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });

        if (respuesta.status === 200) {
            const data = await respuesta.json();
            const contenedor = document.getElementById("contenedor-grupo");
            const titulo = document.getElementById("grupo-titulo");
            const mensaje = document.getElementById("grupo-mensaje");
            const lista = document.getElementById("grupo-lista-miembros");

            titulo.innerText = "";
            mensaje.innerText = "";
            lista.innerHTML = "";
            contenedor.classList.remove("hidden");

            if (data.tieneGrupo) {
                titulo.innerText = "Mi Equipo: " + data.nombreGrupo;
                mensaje.innerText = "Integrantes del grupo:";

                data.miembros.forEach(miembro => {
                    let li = document.createElement("li");
                    li.innerText = miembro;
                    if (miembro === nombreJugador) {
                        li.style.fontWeight = "bold";
                        li.innerText += " (Tú)";
                    }
                    lista.appendChild(li);
                });
            } else {
                titulo.innerText = "Sin Grupo";
                mensaje.innerText = "Actualmente no perteneces a ningún grupo. ¡Únete a uno en la sección de grupos!";
            }
        } else {
            avisar("Error al intentar obtener la información de tu grupo.", true);
        }
    } catch (error) {
        avisar("El servidor de clanes no responde.", true);
    }
}

async function cargarEventos() {
    const contenedor = document.getElementById('contenedor-eventos');
    const jugadorActual = localStorage.getItem("jugadorActual");

    // Solo puedes un evento
    const misionActiva = localStorage.getItem("misionActual_" + jugadorActual);
    if (misionActiva) {
        // Si está en una misión, NO le enseñamos el tablón, le metemos directo al ranking
        cargarRankingEvento(misionActiva);
        return;
    }

    // Si no está en ninguna misión, cargamos el tablón normal
    contenedor.innerHTML = '<p class="texto-vacio">Buscando pergaminos en el tablón...</p>';

    try {
        const res = await fetch(`${URL_BASE}/eventos`);
        if (res.status === 200) {
            const eventos = await res.json();
            contenedor.innerHTML = '';

            if (!eventos || eventos.length === 0) return contenedor.innerHTML = '<p class="texto-vacio">No hay misiones disponibles.</p>';

            eventos.forEach(ev => {
                const img = (ev.imagen_URL && ev.imagen_URL.trim() !== "") ? ev.imagen_URL : "img/sin-imagen.jpg";
                const fechaInicio = new Date(ev.fecha_inicio.replace(/-/g, "/"));
                const ahora = new Date();
                let botonHTML = '';

                if (fechaInicio > ahora) {
                    // Si la fecha del evento es en el futuro (ej. Halloween), se bloquea
                    botonHTML = `<button class="btn-primary" style="background: #222; color: #666; border-color: #444; cursor: not-allowed;" disabled>⏳ PRÓXIMAMENTE</button>`;
                } else {
                    botonHTML = `<button class="btn-primary" onclick="inscribirseEvento('${ev.id}')">INSCRIBIRSE</button>`;
                }

                contenedor.innerHTML += `
                    <div class="tarjeta-grid">
                        <div>
                            <img src="${img}" class="tarjeta-img" onerror="this.src='img/sin-imagen.jpg';">
                            <h4>${ev.nombre}</h4>
                            <p>${ev.descripcion}</p>
                            <p style="color:var(--gold-dim); font-size:0.9rem;">⏳ ${ev.fecha_inicio.split(' ')[0]} - ${ev.fecha_fin.split(' ')[0]}</p>
                        </div>
                        ${botonHTML}
                    </div>`;
            });
        }
    } catch (e) { contenedor.innerHTML = '<p class="texto-vacio">Servidor de misiones caído.</p>'; }
}

async function inscribirseEvento(idEvento) {
    const username = localStorage.getItem("jugadorActual");
    try {
        const res = await fetch(`${URL_BASE}/eventos/inscripcion`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ username, idEvento })
        });

        if (res.status === 201 || res.status === 200) {
            avisar('¡Te has unido a la misión!', false);

            localStorage.setItem("misionActual_" + username, idEvento);
        } else {
            // Si falla porque ya estaba en la Base de Datos, lo guardamos localmente para arreglar el bug visual
            localStorage.setItem("misionActual_" + username, idEvento);
        }

        cargarRankingEvento(idEvento);

    } catch (e) { avisar("Error al inscribirse.", true); }
}

async function cargarRankingEvento(idEvento) {
    const cont = document.getElementById('contenedor-eventos');
    cont.innerHTML = '<p class="texto-vacio">Cargando clasificación de la misión...</p>';
    try {
        const res = await fetch(`${URL_BASE}/eventos/${idEvento}/ranking`);
        if (res.status === 200) {
            // Le pasamos también el idEvento para que la función de dibujar sepa dónde estamos
            dibujarRanking(await res.json(), idEvento);
        } else {
            cont.innerHTML = '<p class="texto-vacio">Aún no hay clasificación.</p>';
        }
    } catch (e) { cont.innerHTML = '<p class="texto-vacio">Error de conexión.</p>'; }
}

function dibujarRanking(lista, idEvento) {
    const cont = document.getElementById('contenedor-eventos');
    const yo = localStorage.getItem("jugadorActual");

    let html = `
        <div class="ranking-caja" style="grid-column: 1 / -1;">
            <h2 style="text-align:center; color:var(--gold); font-family:'Cinzel';">🏆 CLASIFICACIÓN DE LA MISIÓN 🏆</h2>
            <ul class="ranking-lista">
    `;

    if (lista.length === 0) {
        html += `<li class="texto-vacio" style="margin-bottom: 20px;">Nadie ha puntuado aún. ¡Sé el primero!</li>`;
    } else {
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
    }

const estoyEnMision = localStorage.getItem("misionActual_" + yo);

    if (estoyEnMision) {
        html += `</ul>
            <div style="display: flex; flex-direction: column; gap: 12px; margin-top:25px;">
                <button class="btn-primary" onclick="cargarRankingEvento('${idEvento}')">🔄 ACTUALIZAR CLASIFICACIÓN</button>
                <button class="btn-primary" style="border-color: var(--danger); color: var(--danger);" onclick="salirDelEvento('${idEvento}')">❌ ABANDONAR MISIÓN</button>
            </div>
        </div>`;
    } else {
        html += `</ul><button class="btn-primary" style="margin-top:25px;" onclick="cargarEventos()">⬅ VOLVER AL TABLÓN</button></div>`;
    }

    cont.innerHTML = html;
}

async function salirDelEvento(idEvento) {
    const jugador = localStorage.getItem("jugadorActual");

    // Alerta de confirmación del navegador
    const seguro = confirm("⚠️ ¿Estás seguro de que quieres abandonar la misión? Si te vas, tu plaza quedará libre.");
    if (!seguro) return;

    // Liberamos al usuario visualmente borrándolo de la memoria del navegador
    localStorage.removeItem("misionActual_" + jugador);

    // Intentamos avisar a Java para que lo borre de verdad de MariaDB
    try {
        await fetch(`${URL_BASE}/eventos/inscripcion/${jugador}/${idEvento}`, { method: 'DELETE' });
    } catch (e) {
        console.log("El servidor aún no maneja el DELETE, pero se ha expulsado visualmente.");
    }

    avisar("Has abandonado la misión.", false);

    // Al cargar eventos ahora, como ya no está en la misión, volverá a ver el tablón normal
    cargarEventos();
}