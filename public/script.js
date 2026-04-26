const URL_BASE = "http://localhost:8080/api/juego";

async function hacerRegistro() {
    // Datos del HTML
    const nombre = document.getElementById('reg-nombre').value;
    const password = document.getElementById('reg-pass').value;
    const correo = document.getElementById('reg-correo').value;
    // const telefono = document.getElementById('reg-telefono').value;

    // No enviar campos vacíos
    if (!nombre || !password) {
        avisar("¡Nombre y contraseña son obligatorios!", true);
        return;
    }

    // Creamos objeto formato JSON
    const datosUsuario = {
        nombre: nombre,
        password: password,
        correo: correo,
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

        // 4. Analizamos qué nos dice Java
        if (respuesta.status === 201) {
            avisar("¡Explorador registrado con éxito!", false);

            // Traducimos el paquete JSON que nos mandó Java
            const datosUsuario = await respuesta.json();

            // Guardamos el nombre del jugador para usarlo en la tienda/juego
            localStorage.setItem("jugadorActual", nombre);

            // Configuramos la pantalla con la VERDAD de Java
            document.getElementById("mensaje-bienvenida").innerText = "CAMPAMENTO DE " + datosUsuario.nombre.toUpperCase();
            document.getElementById("perfil-nombre").innerText = datosUsuario.nombre;

            // ¡LEEMOS LAS MONEDAS DESDE JAVA!
            document.getElementById("contador-monedas").innerText = datosUsuario.monedas;

            // Cargamos la mochila (que vendrá vacía desde Java, como debe ser)
            cargarMochilaDesdeJava(datosUsuario.inventario);

            // Ocultamos la caja de registro y mostramos la tienda directamente
            document.getElementById("seccion-registro").classList.add("hidden");
            document.getElementById("seccion-dashboard").classList.remove("hidden");

            // Se abre primero el perfil del usuario
            cambiarPestana('tab-perfil', 'btn-perfil');

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
            headers: {
                'Content-Type': 'application/json',
                'Accept':'application/json'
            },
            body: JSON.stringify(credenciales)
        });

        if (respuesta.status === 200) {
            avisar("¡Acceso concedido! Abriendo el campamento...", false);

            // 1. Traducimos el paquete JSON que nos mandó Java
            const datosUsuario = await respuesta.json();
            console.log("Datos cargados desde Java:", datosUsuario);

            // 2. Guardamos el nombre en la "mochila" del navegador
            localStorage.setItem("jugadorActual", datosUsuario.nombre);

            // 3. Actualizamos toda la información visual en el HTML
            document.getElementById("mensaje-bienvenida").innerText = "CAMPAMENTO DE " + datosUsuario.nombre.toUpperCase();
            document.getElementById("perfil-nombre").innerText = datosUsuario.nombre;

            // cargamos las monedas
            document.getElementById("contador-monedas").innerText = datosUsuario.monedas;

            // 4. Cargamos los objetos que ya tenía comprados
            cargarMochilaDesdeJava(datosUsuario.inventario);

            // 5. Ocultamos el login y abrimos el Dashboard en la pestaña Perfil
            document.getElementById("seccion-login").classList.add("hidden");
            document.getElementById("seccion-dashboard").classList.remove("hidden");
            cambiarPestana('tab-perfil', 'btn-perfil');

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

// Función que dibuja el inventario que nos manda Java al iniciar sesión
function cargarMochilaDesdeJava(listaInventario) {
    const cajaInventario = document.getElementById("tab-inventario");

    // Si la lista no existe o está vacía (0 objetos)
    if (!listaInventario || listaInventario.length === 0) {
        cajaInventario.innerHTML = `
            <h3 style="color: white; text-align: center;">Tus Pertinencias</h3>
            <p style="color: #aaa; text-align: center;">Tu mochila está vacía por ahora.</p>`;
        return;
    }

    // Si tiene objetos, creamos la lista visual con todos ellos
    let htmlLista = `
        <h3 style="color: white; text-align: center;">Tus Pertinencias</h3>
        <ul id="lista-mochila" style="color: gold; font-size: 1.1em; list-style-type: none; padding: 0; text-align: center;">`;

    // Recorremos los objetos que envió Java y los pintamos
    listaInventario.forEach(objeto => {
        htmlLista += `<li style="margin-bottom: 10px; background: rgba(0,0,0,0.5); padding: 5px; border-radius: 5px;">✨ ${objeto}</li>`;
    });

    htmlLista += `</ul>`;

    // Inyectamos el HTML en la caja del inventario
    cajaInventario.innerHTML = htmlLista;
}