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

    // 1. CORRECCIÓN: Todo dentro de las llaves del if
    if (!nombre || !password) {
        avisar("Rellena tus credenciales, explorador.", true);
        return; // Ahora solo corta la ejecución si falta algún campo
    }

    const credenciales = {
        nombre: nombre,
        password: password
    };

    try {
        // 2. CORRECCIÓN: El fetch envuelve correctamente a los parámetros y se cierra al final
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
    // 1. Sacamos el nombre del jugador de nuestra "mochila temporal"
    const nombreJugador = localStorage.getItem("jugadorActual");

    // 2. Preparamos el paquete de datos (Igual a la clase PeticionCompra de Java)
    const datosCompra = {
        nombreJugador: nombreJugador,
        nombreObjeto: nombreObjeto,
        precio: precio
    };

    try {
        // 3. Enviamos los datos al nuevo endpoint /comprar
        const respuesta = await fetch(`${URL_BASE}/comprar`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(datosCompra)
        });

        // 4. Analizamos la respuesta del cajero
        if (respuesta.status === 200) {
            avisar("¡Has comprado " + nombreObjeto + "!", false);

            // Actualizamos el contador de monedas en el HTML
            let monedasActuales = parseInt(document.getElementById("contador-monedas").innerText);
            document.getElementById("contador-monedas").innerText = monedasActuales - precio;

            // Añadimos visualmente el objeto a la mochila
            actualizarMochilaHTML(nombreObjeto);

        } else if (respuesta.status === 402) {
            avisar("No tienes suficientes monedas para " + nombreObjeto + ".", true);
        } else {
            avisar("Error en la transacción.", true);
        }
    } catch (error) {
        avisar("El servidor de la tienda está caído.", true);
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
    // 1. Ocultamos todas las cajas
    document.getElementById('tab-tienda').style.display = 'none';
    document.getElementById('tab-inventario').style.display = 'none';
    document.getElementById('tab-perfil').style.display = 'none';

    // 2. Le quitamos el color 'activo' a todos los botones
    document.getElementById('btn-tienda').classList.remove('active');
    document.getElementById('btn-inventario').classList.remove('active');
    document.getElementById('btn-perfil').classList.remove('active');

    // 3. Mostramos la caja pedida y encendemos su botón
    document.getElementById(idPestana).style.display = 'block';
    document.getElementById(idBoton).classList.add('active');

    // Borramos cualquier mensaje que hubiera en pantalla
    document.getElementById('mensaje-sistema').innerText = "";
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
