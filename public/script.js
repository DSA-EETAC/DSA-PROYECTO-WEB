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
    contenedor.innerHTML = '<p style="text-align: center; color: white;">Buscando pergaminos en el tablón...</p>';

    try {
        const respuesta = await fetch(`${URL_BASE}/eventos`, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });

        if (respuesta.status === 200) {
            const eventos = await respuesta.json();
            contenedor.innerHTML = '';

            if (!eventos || eventos.length === 0) {
                contenedor.innerHTML = '<p style="color: #aaa; text-align: center;">No hay misiones disponibles actualmente.</p>';
                return;
            }

            eventos.forEach(evento => {
                console.log("EVENTO RECIBIDO:", evento);

                const imgValida = (evento.imagen_URL && evento.imagen_URL.trim() !== "" && evento.imagen_URL !== "undefined")
                    ? evento.imagen_URL
                    : "img/sin-imagen.jpg";

                // Estilos limpios y distribuidos sin anchos fijos para CSS Grid
                const tarjetaHtml = `
                    <div style="background: rgba(0,0,0,0.8); border: 1px solid #e67e22; padding: 15px; border-radius: 8px; text-align: center; box-shadow: 0 4px 8px rgba(0,0,0,0.5); display: flex; flex-direction: column; justify-content: space-between;">
                        <img src="${imgValida}"
                             alt="${evento.nombre}"
                             onerror="this.onerror=null; this.src='img/sin-imagen.jpg';"
                             style="width: 100%; height: 140px; object-fit: cover; border-radius: 4px; border: 1px solid #444; margin-bottom: 10px;">

                        <h4 style="color: gold; margin: 0 0 8px 0; font-family: 'Cinzel', serif;">
                            ${evento.nombre}
                        </h4>

                        <p style="color: #ddd; font-size: 0.9em; margin-bottom: 12px; line-height: 1.4;">
                            ${evento.descripcion}
                        </p>

                        <p style="color: #aaa; font-size: 0.8em; margin-bottom: 15px;">
                            ⏳ ${evento.fecha_inicio ? evento.fecha_inicio.split(' ')[0] : 'Hoy'}
                            al ${evento.fecha_fin ? evento.fecha_fin.split(' ')[0] : '...'}
                        </p>

                        <button class="btn-primary"
                                style="padding: 8px 15px; font-size: 0.9em;"
                                onclick="inscribirseEvento('${evento.id}')">
                            INSCRIBIRSE
                        </button>
                    </div>
                `;
                contenedor.innerHTML += tarjetaHtml;
            });
        } else {
            contenedor.innerHTML = '<p style="color: #ff4444; text-align: center;">Error al contactar con el tablón de misiones.</p>';
        }
    } catch (error) {
        console.error('Error cargando eventos:', error);
        contenedor.innerHTML = '<p style="color: #ff4444; text-align: center;">El servidor de misiones está caído.</p>';
    }
}

async function inscribirseEvento(idDelEvento) {
    console.log("🚀 PASO 1: Botón presionado. Intentando inscribir en:", idDelEvento);
    const nombreUsuario = localStorage.getItem("jugadorActual");

    if (!nombreUsuario || nombreUsuario === "undefined") {
        avisar("¡Debes estar logueado para inscribirte!", true);
        return;
    }

    const requestData = { username: nombreUsuario, idEvento: idDelEvento };

    try {
        const respuesta = await fetch(`${URL_BASE}/eventos/inscripcion`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify(requestData)
        });

        if (respuesta.status === 201 || respuesta.status === 200) {
            console.log("🚀 PASO 2: Inscripción nueva OK. Cargando ranking...");
            try { avisar('¡Inscripción realizada! Cargando clasificación...', false); } catch(e){}
            cargarRankingEvento(idDelEvento);
        } else {
            console.log("🚀 PASO 2 (Alt): Ya inscrito. Cargando ranking actual...");
            try { avisar('Ya estás apuntado. Viendo clasificación actual...', false); } catch(e){}
            cargarRankingEvento(idDelEvento);
        }
    } catch (error) {
        console.error('❌ ERROR en la inscripción:', error);
        avisar("Fallo de conexión al enviar la inscripción.", true);
    }
}

async function cargarRankingEvento(idEvento) {
    console.log("🏆 RANKING PASO 1: Pidiendo datos para el evento:", idEvento);
    const contenedor = document.getElementById('contenedor-eventos');

    if (contenedor) {
        contenedor.innerHTML = '<p style="color: white; text-align: center;">Calculando puntuaciones...</p>';
    }

    try {
        const respuesta = await fetch(`${URL_BASE}/eventos/${idEvento}/ranking`, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });

        if (respuesta.status === 200) {
            const ranking = await respuesta.json();
            console.log("🏆 RANKING PASO 2: Datos recibidos:", ranking);
            dibujarRanking(ranking);
        } else {
            if (contenedor) contenedor.innerHTML = '<p style="color: #ff4444; text-align: center;">Error al cargar la clasificación.</p>';
        }
    } catch (error) {
        console.error('Error cargando el ranking:', error);
    }
}

function dibujarRanking(listaJugadores) {
    const contenedor = document.getElementById('contenedor-eventos');
    const jugadorActual = localStorage.getItem("jugadorActual");

    let htmlRanking = `
        <div style="background: rgba(0,0,0,0.9); border: 2px solid gold; padding: 20px; border-radius: 10px; width: 100%; max-width: 600px; margin: 0 auto; text-align: center;">
            <h2 style="color: gold; font-family: 'Cinzel', serif; margin-bottom: 20px;">🏆 CLASIFICACIÓN 🏆</h2>
            <ul style="list-style: none; padding: 0; color: white; font-size: 1.1em; text-align: left;">
    `;

    if (listaJugadores.length === 0) {
        htmlRanking += `<li style="text-align: center;">Nadie ha puntuado aún. ¡Sé el primero!</li></ul></div>`;
        contenedor.innerHTML = htmlRanking;
        return;
    }

    // Top 5
    const top5 = listaJugadores.slice(0, 5);
    let yoEstoyEnElTop5 = false;

    top5.forEach((jugador, index) => {
        let medalla = "🏅";
        let colorFondo = "transparent";

        if (index === 0) { medalla = "🥇"; colorFondo = "rgba(255, 215, 0, 0.2)"; }
        else if (index === 1) { medalla = "🥈"; colorFondo = "rgba(192, 192, 192, 0.2)"; }
        else if (index === 2) { medalla = "🥉"; colorFondo = "rgba(205, 127, 50, 0.2)"; }

        let textoNombre = jugador.nombreJugador;
        if (jugador.nombreJugador === jugadorActual) {
            yoEstoyEnElTop5 = true;
            textoNombre = `<span style="color: #2ecc71;">${jugador.nombreJugador} (Tú)</span>`;
            colorFondo = "rgba(46, 204, 113, 0.2)";
        }

        htmlRanking += `
            <li style="margin: 5px 0; padding: 10px; border-bottom: 1px solid #444; background: ${colorFondo}; border-radius: 5px; display: flex; justify-content: space-between;">
                <span>${medalla} <strong>${textoNombre}</strong></span>
                <span style="color: #ccc;">${jugador.puntuacion} pts</span>
            </li>
        `;
    });

    // Si estás fuera del Top 5, te mostramos abajo
    if (!yoEstoyEnElTop5) {
        const miIndice = listaJugadores.findIndex(j => j.nombreJugador === jugadorActual);
        if (miIndice !== -1) {
            const miPuntuacion = listaJugadores[miIndice].puntuacion;
            const miPosicionReal = miIndice + 1;

            htmlRanking += `
                <li style="text-align: center; color: #888; margin: 10px 0;">...</li>
                <li style="margin: 5px 0; padding: 10px; border: 1px dashed #2ecc71; background: rgba(46, 204, 113, 0.1); border-radius: 5px; display: flex; justify-content: space-between;">
                    <span>${miPosicionReal}º <strong><span style="color: #2ecc71;">${jugadorActual} (Tú)</span></strong></span>
                    <span style="color: #ccc;">${miPuntuacion} pts</span>
                </li>
            `;
        }
    }

    htmlRanking += `
            </ul>
            <button class="btn-primary" style="margin-top: 25px; padding: 10px 20px;" onclick="cargarEventos()">⬅ VOLVER AL TABLÓN</button>
        </div>
    `;

    contenedor.innerHTML = htmlRanking;
}