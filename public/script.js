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
        return; // Cortamos la ejecución aquí
    }

    // Creamos objeto formato JSON
    const datosUsuario = {
        nombre: nombre,
        password: password,
        mail: correo
        //telefono: telefono
    };

    try {
        // Enviamos petición POST a backend
        const respuesta = await fetch(`${URL_BASE}/registro`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json',
                'Accept' : 'application/json'
            },
            body: JSON.stringify(datosUsuario)
        });

        // 4. Analizamos qué nos dice BackEnd
        if (respuesta.status === 201) {
            avisar("¡Explorador registrado con éxito!", false);
            const datosUsuario = await respuesta.json();

            localStorage.setItem("jugadorActual", nombre);
            document.getElementById("mensaje-bienvenida").innerText = "CAMPAMENTO DE " + datosUsuario.nombre.toUpperCase();
            document.getElementById("perfil-nombre").innerText = datosUsuario.nombre;
            document.getElementById("contador-monedas").innerText = datosUsuario.monedas;

            await actualizarMochilaDesdeBaseDeDatos(datosUsuario.nombre);

            //Limpiar campos de register
            document.getElementById('reg-nombre').value = "";
            document.getElementById('reg-pass').value = "";
            document.getElementById('reg-pass-confirm').value = "";
            document.getElementById('reg-correo').value = "";

            document.getElementById("seccion-registro").classList.add("hidden");
            document.getElementById("seccion-dashboard").classList.remove("hidden");

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
        return; // Ahora solo corta la ejecución si falta algún campo
    }

    const credenciales = {
        nombre: nombre,
        password: password
    };

    try {
        const respuesta = await fetch(`${URL_BASE}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(credenciales)
        }); // <-- Ahora se cierra en el lugar correcto

        if (respuesta.status === 200) {
            avisar("¡Acceso concedido! Abriendo el campamento...", false);
            const datosUsuario = await respuesta.json();
            console.log("Datos cargados desde Java:", datosUsuario);

            localStorage.setItem("jugadorActual", datosUsuario.nombre);

            document.getElementById("mensaje-bienvenida").innerText = "CAMPAMENTO DE " + datosUsuario.nombre.toUpperCase();
            document.getElementById("perfil-nombre").innerText = datosUsuario.nombre;
            document.getElementById("contador-monedas").innerText = datosUsuario.monedas;

            await actualizarMochilaDesdeBaseDeDatos(datosUsuario.nombre);

            document.getElementById("seccion-login").classList.add("hidden");
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
            const listaItems = inventarioJugador.objetos; // Esto recibe el array de Strings de Java
            cargarMochilaDesdeJava(listaItems); // Se lo pasamos a la función que pinta en HTML
        }

    } catch (error) {
        console.error("Error al recuperar el inventario relacional:", error);
    }
}


// Cambio visual entre Login y Registro
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

// Muestra mensajes en la pantalla (Dorado OK, Rojo error)
function avisar(texto, esError) {
    const divMensaje = document.getElementById('mensaje-sistema');
    divMensaje.innerText = texto;
    divMensaje.style.color = esError ? "#ff4444" : "#ffcc33";
}


// Función para comprar objetos en el Tienda
async function comprarItem(nombreObjeto, precio) {
    const nombreJugador = localStorage.getItem("jugadorActual");
// 1. CLÁUSULAS DE PROTECCIÓN (Guard Clauses)
    // Si no hay usuario, o el usuario es "undefined", no hacemos nada.
    if (!nombreJugador || nombreJugador === "undefined" || nombreJugador === "null") {
        console.error("Error: No hay un usuario logueado en localStorage.");
        avisar("Debes iniciar sesión para comprar.", true);
        return;
    }

    // Comprobamos que URL_BASE existe (evita errores 404 por variables vacías)
    if (typeof URL_BASE === 'undefined') {
        console.error("Error crítico: URL_BASE no está definida.");
        avisar("Error de configuración del cliente.", true);
        return;
    }

    // 2. Construcción del paquete
    const datosCompra = {
        nombreJugador: nombreJugador,
        nombreObjeto: nombreObjeto,
        precio: precio
    };

    console.log("Enviando compra a:", `${URL_BASE}/comprar`, "Datos:", datosCompra);

    try {
        // 3. Envío al servidor
        const respuesta = await fetch(`${URL_BASE}/comprar`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(datosCompra)
        });

        // 4. Manejo de estados de respuesta
        if (respuesta.status === 200) {
            avisar("¡Has comprado " + nombreObjeto + "!", false);

            // Actualización del contador en la interfaz
            const contador = document.getElementById("contador-monedas");
            if (contador) {
                let monedasActuales = parseInt(contador.innerText) || 0;
                contador.innerText = monedasActuales - precio;
            }

            // Añadir a la mochila (asegúrate de que esta función exista)
            if (typeof actualizarMochilaHTML === 'function') {
                actualizarMochilaHTML(nombreObjeto);
            }

        } else if (respuesta.status === 402) {
            avisar("No tienes suficientes monedas para " + nombreObjeto + ".", true);
        } else {
            // Captura errores inesperados del servidor (ej. 500)
            avisar("Error en la transacción. Código: " + respuesta.status, true);
        }
    } catch (error) {
        console.error("Error en el fetch:", error);
        avisar("El servidor de la tienda está caído o no responde.", true);
    }
}

// Función extra para pintar el objeto en la pestaña de la mochila
function actualizarMochilaHTML(nuevoObjeto) {
    const cajaInventario = document.getElementById("tab-inventario");

    // Si la mochila está vacía, borramos el texto y preparamos una lista
    if (cajaInventario.innerHTML.includes("Tu mochila está vacía por ahora.")) {
        cajaInventario.innerHTML = `<h3 style="color: white; text-align: center;">Tus Pertinencias</h3><ul id="lista-mochila" style="color: gold; font-size: 1.1em;"></ul>`;
    }

    // Añadimos el nuevo objeto a la lista
    const lista = document.getElementById("lista-mochila");
    lista.innerHTML += `<li>✨ ${nuevoObjeto}</li>`;
}

// Función mejorada para cambiar entre pestañas

function cambiarPestana(idPestana, idBoton) {
    // 1. Ocultamos TODAS las cajas (¡Incluida la de eventos!)
    document.getElementById('tab-tienda').style.display = 'none';
    document.getElementById('tab-inventario').style.display = 'none';
    document.getElementById('tab-perfil').style.display = 'none';
    document.getElementById('tab-grupos').style.display = 'none';
    document.getElementById('tab-eventos').style.display = 'none';

    // 2. Le quitamos el color 'activo' a TODOS los botones
    document.getElementById('btn-tienda').classList.remove('active');
    document.getElementById('btn-inventario').classList.remove('active');
    document.getElementById('btn-perfil').classList.remove('active');
    document.getElementById('btn-grupos').classList.remove('active');
    document.getElementById('btn-eventos').classList.remove('active');

    // 3. Mostramos la caja pedida y encendemos su botón
    document.getElementById(idPestana).style.display = 'block';
    document.getElementById(idBoton).classList.add('active');

    // Borramos cualquier mensaje que hubiera en pantalla
    document.getElementById('mensaje-sistema').innerText = "";

    // 4. Cargamos la información del servidor según la pestaña abierta
    if (idPestana === 'tab-grupos') {
        cargarGrupos();
    }
    if (idPestana === 'tab-eventos') {
        cargarEventos(); // ¡Esto es lo que hace que deje de poner "Buscando pergaminos..."!
    }
}

// Función para cerrar sesión y volver a la pantalla de inicio
function cerrarSesion() {
    localStorage.removeItem("jugadorActual");

    // Ocultamos el dashboard y mostramos el login
    document.getElementById("seccion-dashboard").classList.add("hidden");
    document.getElementById("seccion-login").classList.remove("hidden");

    // Limpiamos los campos de texto
    document.getElementById("login-nombre").value = "";
    document.getElementById("login-pass").value = "";
}

// Funcion que dibuja el inventario
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

// Función para pedir los ítems al servidor
function cargarTienda() {
    fetch(`${URL_BASE}/tienda`)
        .then(response => {
            if (!response.ok) {
                throw new Error("Error en la respuesta del servidor");
            }
            return response.json();
        })
        .then(datosTienda => {
            // ¡OJO AQUÍ! Extraemos la lista 'items' de dentro del objeto
            const listaItems = datosTienda.items;
            dibujarTienda(listaItems);
        })
        .catch(error => {
            console.error('Error al cargar la tienda:', error);
            const contenedor = document.getElementById('contenedor-items-tienda');
            contenedor.innerHTML = '<p style="color: #ff4444; text-align: center;">Error de conexión con el mercader.</p>';
        });
}

// Función que pinta el HTML de la tienda
function dibujarTienda(listaItems) {
    const contenedor = document.getElementById('contenedor-items-tienda');
    contenedor.innerHTML = ''; // Vaciamos por si acaso

    if (!listaItems || listaItems.length === 0) {
        contenedor.innerHTML = '<p style="color: #aaa; text-align: center;">El mercader no tiene existencias hoy.</p>';
        return;
    }

    // Recorremos el array de JSON (cada item tiene id, nombre, precio, tipo)
    listaItems.forEach(item => {
        // Ponemos el item.id a comprarItem en lugar del nombre.
        const tarjetaHtml = `
            <div style="flex: 1; min-width: 150px; border: 1px solid rgba(255,255,255,0.2); padding: 10px; text-align: center; background: rgba(0,0,0,0.5);">
                <h4 style="color: white;">${item.nombre}</h4>
                <p style="color: gold;">${item.precio} 🪙</p>
                <p style="color: #ccc; font-size: 0.8em; margin-bottom: 8px;">${item.tipo}</p>
                <button class="btn-primary" style="padding: 5px; font-size: 0.8em;" onclick="comprarItem('${item.nombre}', ${item.precio})">COMPRAR</button>
            </div>
        `;
        contenedor.innerHTML += tarjetaHtml;
    });
}

// Llamar a cargarTienda() cuando la página arranque
window.onload = function() {
    cargarTienda();
};

// Función para pedir los grupos al servidor
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

// Función para pintar el HTML de los grupos
function dibujarGrupos(listaGrupos) {
    const contenedor = document.getElementById('contenedor-grupos');
    contenedor.innerHTML = ''; // Vaciamos primero

    if (!listaGrupos || listaGrupos.length === 0) {
        contenedor.innerHTML = '<p style="color: #aaa; text-align: center;">No hay grupos disponibles ahora mismo.</p>';
        return;
    }

    listaGrupos.forEach(grupo => {
        const tarjetaHtml = `
            <div style="flex: 1; min-width: 150px; border: 1px solid rgba(255,255,255,0.2); padding: 10px; text-align: center; background: rgba(0,0,0,0.5);">
                <h4 style="color: white;">${grupo.nombre}</h4>
                <p style="color: #ccc; font-size: 0.8em; margin-bottom: 8px;">ID: ${grupo.id}</p>
                <button class="btn-primary" style="padding: 5px; font-size: 0.8em; background-color: #27ae60; border-color: #2ecc71;" 
                onclick="unirseGrupo('${grupo.id}', '${grupo.nombre}')">UNIRSE</button>
            </div>
        `;
        contenedor.innerHTML += tarjetaHtml;
    });
}

// Función para unirse a un grupo
async function unirseGrupo(idGrupo, nombreGrupo) {
    const nombreJugador = localStorage.getItem("jugadorActual");

    // Tu backend actual pide un objeto User en el body del POST para unirse
    const datosUsuario = {
        nombre: nombreJugador
    };

    try {
        const respuesta = await fetch(`${URL_BASE}/grupos/${idGrupo}/unirse`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
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
    // Usamos la misma clave que usas en unirseGrupo
    const nombreJugador = localStorage.getItem("jugadorActual");

    if (!nombreJugador) {
        avisar("No se ha detectado ninguna sesión activa.", true);
        return;
    }

    try {
        // Hacemos la petición GET usando tu URL_BASE
        const respuesta = await fetch(`${URL_BASE}/usuarios/${nombreJugador}/grupo`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (respuesta.status === 200) {
            // Transformamos la respuesta a JSON
            const data = await respuesta.json();

            // Obtenemos los elementos del HTML
            const contenedor = document.getElementById("contenedor-grupo");
            const titulo = document.getElementById("grupo-titulo");
            const mensaje = document.getElementById("grupo-mensaje");
            const lista = document.getElementById("grupo-lista-miembros");

            // Limpiamos el contenido anterior
            titulo.innerText = "";
            mensaje.innerText = "";
            lista.innerHTML = "";

            // Mostramos el contenedor (quitando la clase que lo oculta)
            contenedor.classList.remove("hidden");

            if (data.tieneGrupo) {
                // SÍ TIENE GRUPO
                titulo.innerText = "Mi Equipo: " + data.nombreGrupo;
                mensaje.innerText = "Integrantes del grupo:";

                data.miembros.forEach(miembro => {
                    let li = document.createElement("li");
                    li.innerText = miembro;

                    // Resaltamos en negrita si es el propio jugador
                    if (miembro === nombreJugador) {
                        li.style.fontWeight = "bold";
                        li.innerText += " (Tú)";
                    }
                    lista.appendChild(li);
                });
            } else {
                // NO TIENE GRUPO
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
    try {
        const respuesta = await fetch(`${URL_BASE}/eventos`, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });

        if (respuesta.status === 200) {
            const eventos = await respuesta.json();
            const contenedor = document.getElementById('contenedor-eventos');
            contenedor.innerHTML = ''; // Limpiamos el texto de "Buscando pergaminos..."

            // Recorremos la lista de eventos que nos da MariaDB
            eventos.forEach(evento => {
                            // Si la imagen está vacía, usamos una de relleno
                            const imagenValida = (evento.imagen && evento.imagen.trim() !== "" && evento.imagen !== "undefined")
                                                 ? evento.imagen
                                                 : 'https://via.placeholder.com/280x140?text=Sin+Imagen';

                            const tarjetaHtml = `
                                <div style="background: rgba(0,0,0,0.8); border: 1px solid #e67e22; padding: 15px; border-radius: 8px; width: 280px; text-align: center; box-shadow: 0 4px 8px rgba(0,0,0,0.5);">
                                    <img src="${imagenValida}" alt="${evento.nombre}" style="width: 100%; height: 140px; object-fit: cover; border-radius: 4px; border: 1px solid #444; margin-bottom: 10px;">
                                    <h4 style="color: gold; margin: 0 0 8px 0; font-family: 'Cinzel', serif;">${evento.nombre}</h4>
                                    <p style="color: #ddd; font-size: 0.9em; margin-bottom: 12px; line-height: 1.4;">${evento.descripcion}</p>
                                    <p style="color: #aaa; font-size: 0.8em; margin-bottom: 15px;">⏳ ${evento.fecha_inicio.split(' ')[0]} al ${evento.fecha_fin.split(' ')[0]}</p>
                                    <button class="btn-primary" style="padding: 8px 15px; font-size: 0.9em;" onclick="inscribirseEvento('${evento.id}')">INSCRIBIRSE</button>
                                </div>
                            `;
                            contenedor.innerHTML += tarjetaHtml;
                        });


        } else {
            document.getElementById('contenedor-eventos').innerHTML = '<p style="color: #ff4444; text-align: center;">Error al cargar las misiones.</p>';
        }
    } catch (error) {
        console.error('Error cargando eventos:', error);
        document.getElementById('contenedor-eventos').innerHTML = '<p style="color: #ff4444; text-align: center;">El tablón de misiones está caído.</p>';
    }
}

// Función para cuando el usuario hace clic en el botón de Inscribirse
async function inscribirseEvento(idDelEvento) {
    const nombreUsuario = localStorage.getItem("jugadorActual");

    if (!nombreUsuario) {
        avisar("¡Debes estar logueado para inscribirte!", true);
        return;
    }

    const requestData = {
        username: nombreUsuario,
        idEvento: idDelEvento
    };

    try {
        const respuesta = await fetch(`${URL_BASE}/eventos/inscripcion`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(requestData)
        });

        if (respuesta.status === 201 || respuesta.status === 200) {
            avisar('¡Inscripción realizada con éxito! Nos vemos en el evento.', false);
        } else {
            avisar('No se pudo realizar la inscripción. ¿Ya estás apuntado a este evento?', true);
        }
    } catch (error) {
        console.error('Error en la inscripción:', error);
        avisar("Fallo de conexión al enviar la inscripción.", true);
    }
}
