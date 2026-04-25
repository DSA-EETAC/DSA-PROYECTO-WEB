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
            // Esperamos 2 segundos y volvemos al login automáticamente
            setTimeout(() => cambiarVista('login'), 2000);
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
            headers: { 'Content-Type': 'application/json',
            'Accept':'application/json'
            },
            body: JSON.stringify(credenciales)
        });

        if (respuesta.status === 200) {
            avisar("¡Acceso concedido! Preparando expedición...", false);

            // Guardamos el nombre del jugador para usarlo en Unity después
            localStorage.setItem("jugadorActual", nombre);

            // Simulación de salto a Unity (aquí pondrás la URL de tu juego)
            setTimeout(() => {
                alert("¡Bienvenido " + nombre + "! Ahora saltaríamos al juego en Unity.");
                // window.location.href = "juego_unity.html";
            }, 1500);

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