// lgica de modales y formulario de solicitudes

let _debounceTimerRequest = null;

// lee los tipos de servicio desde el data-attribute
function cargarTiposServicio() {
    try {
        const el = document.getElementById("serviciosData");
        if (!el) return [];
        const json = el.getAttribute("data-servicios");
        if (!json || json.trim() === "") return [];
        const parsed = JSON.parse(json);
        return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
        console.error("Error al parsear tipos de servicio:", e);
        return [];
    }
}

// HELPERS MODALES

function actualizarCliente(select) {
    const op = select.options[select.selectedIndex];
    document.getElementById("clienteIdHidden").value          = op.value;
    document.getElementById("clienteNombreHidden").value      = op.getAttribute("data-nombre");
    document.getElementById("clienteDocumentoHidden").value   = op.getAttribute("data-documento");
    document.getElementById("clienteTipoHidden").value        = op.getAttribute("data-tipo");
    document.getElementById("clienteCorreoHidden").value      = op.getAttribute("data-correo");
    document.getElementById("clienteTelefonoHidden").value    = op.getAttribute("data-telefono");
}

function calcularTotal() {
    const noServicios = parseInt(document.getElementById("noServicios").value) || 0;
    const precio = parseFloat(
        document.querySelector("#tipoServicioSelect option:checked").dataset.precio
    ) || 0;
    const total = precio * noServicios;

    document.getElementById("totalMostrado").value = "$" + total.toLocaleString("es-CO");
    document.getElementById("total").value         = total;
    document.getElementById("servicioPrecioHidden").value = precio;
}

function limpiarHiddenCliente() {
    ["clienteIdHidden","clienteNombreHidden","clienteDocumentoHidden",
     "clienteTipoHidden","clienteCorreoHidden","clienteTelefonoHidden"]
        .forEach(id => {
            const el = document.getElementById(id);
            if (el) el.value = "";
        });
}

function abrirModalAgregarSolicitud() {
    document.getElementById("modalTitleSolicitud").innerText  = "Agregar Solicitud";
    document.getElementById("btnGuardarSolicitud").innerText  = "Guardar";
    document.getElementById("solicitudId").value    = "";
    document.getElementById("fecha").value          = "";
    document.getElementById("hora").value           = "";
    document.getElementById("noServicios").value    = "";
    document.getElementById("descripcion").value    = "";
    document.getElementById("estado").value         = "En espera";
    document.getElementById("contadorDescripcion").innerText = "0 / 150";

    // Limpiar el select visual y los hidden del cliente
    const clienteSelect = document.getElementById("clienteSelect");
    if (clienteSelect) clienteSelect.value = "";
    limpiarHiddenCliente();

    // Ocultar error de cliente si estaba visible
    const errCliente = document.getElementById("errorCliente");
    if (errCliente) errCliente.style.display = "none";

    abrirModal("modalSolicitud");
}

function abrirModalEditarSolicitud(btn) {
    document.getElementById("modalTitleSolicitud").innerText  = "Editar Solicitud";
    document.getElementById("btnGuardarSolicitud").innerText  = "Actualizar";
    document.getElementById("solicitudId").value              = btn.dataset.id;
    document.getElementById("clienteIdHidden").value          = btn.dataset.clienteId       || "";
    document.getElementById("clienteNombreHidden").value      = btn.dataset.clienteNombre   || "";
    document.getElementById("clienteDocumentoHidden").value   = btn.dataset.clienteDocumento|| "";
    document.getElementById("clienteTipoHidden").value        = btn.dataset.clienteTipo     || "";
    document.getElementById("clienteCorreoHidden").value      = btn.dataset.clienteCorreo   || "";
    document.getElementById("clienteTelefonoHidden").value    = btn.dataset.clienteTelefono || "";

    // Sincronizar visualmente el select con el cliente actual
    const clienteSelect = document.getElementById("clienteSelect");
    if (clienteSelect && btn.dataset.clienteId) {
        clienteSelect.value = btn.dataset.clienteId;
        if (!clienteSelect.value) {
            // Fallback: buscar por nombre si el ID no coincide
            Array.from(clienteSelect.options).forEach(opt => {
                if (opt.getAttribute("data-nombre") === btn.dataset.clienteNombre) {
                    clienteSelect.value = opt.value;
                }
            });
        }
    }

    document.getElementById("tipoServicioSelect").value       = btn.dataset.tipoServicio;
    document.getElementById("servicioPrecioHidden").value     = btn.dataset.servicioPrecio;

    document.getElementById("noServicios").value              = btn.dataset.noServicios;
    document.getElementById("fecha").value                    = btn.dataset.fecha;
    document.getElementById("hora").value                     = btn.dataset.hora;
    document.getElementById("descripcion").value              = btn.dataset.descripcion;
    document.getElementById("estado").value                   = btn.dataset.estado;

    // Ocultar error de cliente si estaba visible
    const errCliente = document.getElementById("errorCliente");
    if (errCliente) errCliente.style.display = "none";

    calcularTotal();
    abrirModal("modalSolicitud");
}

function confirmarEliminar(btn) {
    const id = btn.dataset.id;
    const confirmBtn = document.getElementById("confirmDelete");
    const newBtn = confirmBtn.cloneNode(true);
    confirmBtn.parentNode.replaceChild(newBtn, confirmBtn);

    newBtn.addEventListener("click", function () {
        const csrfToken  = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        fetch(`/private/admin/eliminarSolicitud/${id}`, {   // ← corregido
            method: "DELETE",
            headers: { [csrfHeader]: csrfToken }
        })
        .then(async res => {
            const texto = await res.text();
            cerrarModal("deleteModal");

            if (res.ok) {
                location.reload();
            } else if (res.status === 409) {
                setTimeout(() => mostrarErrorEliminar(texto), 150);
            } else {
                setTimeout(() => mostrarErrorEliminar("Error al eliminar la solicitud. Intenta de nuevo."), 150);
            }
        })
        .catch(err => {
            cerrarModal("deleteModal");
            console.error("Error:", err);
        });
    });

    abrirModal("deleteModal");
}

function mostrarErrorEliminar(mensaje) {
    const el = document.getElementById("errorEliminarSolicitud");
    if (!el) return;
    el.textContent = mensaje;
    el.style.display = "block";
    setTimeout(() => {
        el.scrollIntoView({ behavior: "smooth", block: "center" });
    }, 100);
    setTimeout(() => {
        el.style.display = "none";
        el.textContent = "";
    }, 6000);
}

// filtro

function cerrarFiltro() {
    cerrarModal("filterRequestModal");
    setTimeout(() => volverPaso1(), 300);
}

function volverPaso1() {
    document.getElementById("filterStep1").classList.remove("hidden");
    document.getElementById("filterStep2").classList.add("hidden");
    document.getElementById("filterTitle").textContent = "Filtrar por";
}

function mostrarPaso2(tipo) {
    document.getElementById("filterStep1").classList.add("hidden");
    document.getElementById("filterStep2").classList.remove("hidden");

    const titulos = {
        precio       : "Filtrar por precio",
        fecha        : "Filtrar por fecha",
        estado       : "Filtrar por estado",
        tipoServicio : "Filtrar por tipo de servicio"
    };
    document.getElementById("filterTitle").textContent = titulos[tipo] || "Filtrar";

    const opciones = document.getElementById("filterOpciones");
    opciones.innerHTML = "";

    if (tipo === "precio") {
        opciones.innerHTML = `
            <button class="filter-option" id="fo-asc">↑ Menor a mayor</button>
            <button class="filter-option" id="fo-desc">↓ Mayor a menor</button>
        `;
        document.getElementById("fo-asc").addEventListener("click",  () => aplicarFiltroRequest("orden", "asc"));
        document.getElementById("fo-desc").addEventListener("click", () => aplicarFiltroRequest("orden", "desc"));
    }

    if (tipo === "fecha") {
        opciones.innerHTML = `
            <button class="filter-option" id="fo-fasc">↑ Más próxima primero</button>
            <button class="filter-option" id="fo-fdesc">↓ Más lejana primero</button>
        `;
        document.getElementById("fo-fasc").addEventListener("click",  () => aplicarFiltroRequest("orden", "asc",  "fecha"));
        document.getElementById("fo-fdesc").addEventListener("click", () => aplicarFiltroRequest("orden", "desc", "fecha"));
    }

    if (tipo === "estado") {
        opciones.innerHTML = `
            <button class="filter-option" id="fo-espera">En espera</button>
            <button class="filter-option" id="fo-proceso">En proceso</button>
            <button class="filter-option" id="fo-finalizado">Finalizado</button>
        `;
        document.getElementById("fo-espera").addEventListener("click",     () => aplicarFiltroRequest("estado", "En espera"));
        document.getElementById("fo-proceso").addEventListener("click",    () => aplicarFiltroRequest("estado", "En proceso"));
        document.getElementById("fo-finalizado").addEventListener("click", () => aplicarFiltroRequest("estado", "Finalizado"));
    }

    if (tipo === "tipoServicio") {
        const tiposServicioDisponibles = cargarTiposServicio();

        if (tiposServicioDisponibles.length === 0) {
            opciones.innerHTML = `<p class="filter-subtitle">No hay servicios disponibles.</p>`;
            return;
        }

        const wrapper = document.createElement("div");
        wrapper.className = "filter-select-wrapper";

        const select = document.createElement("select");
        select.id        = "fo-tipoServicioSelect";
        select.className = "filter-select";

        const placeholder = document.createElement("option");
        placeholder.value    = "";
        placeholder.textContent = "Seleccione un servicio...";
        placeholder.disabled = true;
        placeholder.selected = true;
        select.appendChild(placeholder);

        tiposServicioDisponibles.forEach(servicio => {
            const opt = document.createElement("option");
            opt.value       = servicio.tipoServicio;
            opt.textContent = servicio.tipoServicio;
            select.appendChild(opt);
        });

        const applyBtn = document.createElement("button");
        applyBtn.className   = "filter-apply-btn";
        applyBtn.textContent = "Aplicar";
        applyBtn.addEventListener("click", () => {
            if (select.value) {
                aplicarFiltroRequest("tipoServicio", select.value);
            }
        });

        select.addEventListener("change", () => {
            applyBtn.disabled = !select.value;
            applyBtn.classList.toggle("filter-apply-btn--active", !!select.value);
        });
        applyBtn.disabled = true;

        wrapper.appendChild(select);
        wrapper.appendChild(applyBtn);
        opciones.appendChild(wrapper);
    }
}

function aplicarFiltroRequest(clave, valor, ordenCampo) {
    cerrarFiltro();
    const url = new URL(window.location.href);
    url.searchParams.delete("orden");
    url.searchParams.delete("ordenCampo");
    url.searchParams.delete("estado");
    url.searchParams.delete("tipoServicio");
    url.searchParams.set(clave, valor);
    if (ordenCampo) url.searchParams.set("ordenCampo", ordenCampo);
    url.searchParams.set("page", "0");
    window.location.href = url.toString();
}

function limpiarFiltro() {
    cerrarFiltro();
    const url = new URL(window.location.href);
    url.searchParams.delete("orden");
    url.searchParams.delete("ordenCampo");
    url.searchParams.delete("estado");
    url.searchParams.delete("tipoServicio");
    url.searchParams.set("page", "0");
    window.location.href = url.toString();
}

// BUSQUEDA

function buscarSolicitudes(termino) {
    const url = new URL(window.location.href);
    if (termino && termino.trim()) {
        url.searchParams.set("busqueda", termino.trim());
    } else {
        url.searchParams.delete("busqueda");
    }
    url.searchParams.set("page", "0");
    window.location.href = url.toString();
}

// VALIDACION DE FECHA Y HORA

function validarFecha(fecha, esEdicion) {
    if (!fecha) return "La fecha es obligatoria.";
    if (!esEdicion) {
        const hoy = new Date(); hoy.setHours(0, 0, 0, 0);
        const sel = new Date(fecha + "T00:00:00");
        if (sel < hoy) return "No se pueden agendar solicitudes en fechas pasadas.";
    }
    const sel = new Date(fecha + "T00:00:00");
    if (sel.getDay() === 0)  return "No se pueden agendar solicitudes los domingos.";
    return null;
}

function validarHora(hora) {
    if (!hora) return "La hora es obligatoria.";
    const [h, m]  = hora.split(":").map(Number);
    const minutos = h * 60 + m;
    if (minutos < 7  * 60)       return "El horario de atención inicia a las 7:00 AM.";
    if (minutos > 18 * 60 + 30)  return "El horario de atención finaliza a las 6:30 PM.";
    return null;
}

function mostrarErrorSolicitud(id, mensaje) {
    const el = document.getElementById(id);
    if (el) { el.textContent = mensaje; el.style.display = "block"; }
}

function limpiarErroresSolicitud() {
    ["errorFecha", "errorHora"].forEach(id => {
        const el = document.getElementById(id);
        if (el) { el.textContent = ""; el.style.display = "none"; }
    });
}

document.addEventListener("DOMContentLoaded", function () {

    document.getElementById("addRequestBtn")
        ?.addEventListener("click", abrirModalAgregarSolicitud);

    document.getElementById("filterRequestBtn")
        ?.addEventListener("click", () => abrirModal("filterRequestModal"));

    document.getElementById("closeModalSolicitud")
        ?.addEventListener("click", () => cerrarModal("modalSolicitud"));

    document.getElementById("closeFilterRequest")
        ?.addEventListener("click", cerrarFiltro);

    document.getElementById("filterByPrecio")
        ?.addEventListener("click", () => mostrarPaso2("precio"));
    document.getElementById("filterByFecha")
        ?.addEventListener("click", () => mostrarPaso2("fecha"));
    document.getElementById("filterByEstado")
        ?.addEventListener("click", () => mostrarPaso2("estado"));
    document.getElementById("filterByTipoServicio")
        ?.addEventListener("click", () => mostrarPaso2("tipoServicio"));
    document.getElementById("filterLimpiar")
        ?.addEventListener("click", limpiarFiltro);
    document.getElementById("filterBack")
        ?.addEventListener("click", volverPaso1);

    document.addEventListener("click", function (e) {
        if (e.target.matches(".action-btn.edit"))   abrirModalEditarSolicitud(e.target);
        if (e.target.matches(".action-btn.delete")) confirmarEliminar(e.target);
    });

    document.getElementById("clienteSelect")
        ?.addEventListener("change", function () { actualizarCliente(this); });

    document.getElementById("tipoServicioSelect")
        ?.addEventListener("change", calcularTotal);
    document.getElementById("noServicios")
        ?.addEventListener("input", calcularTotal);

    document.getElementById("descripcion")
        ?.addEventListener("input", function () {
            document.getElementById("contadorDescripcion").innerText = `${this.value.length} / 150`;
        });

    const searchInput = document.getElementById("searchInput");
    if (searchInput) {
        const params = new URLSearchParams(window.location.search);
        if (params.get("busqueda")) searchInput.value = params.get("busqueda");

        searchInput.addEventListener("input", function () {
            const val = this.value;
            clearTimeout(_debounceTimerRequest);
            _debounceTimerRequest = setTimeout(() => buscarSolicitudes(val), 400);
        });

        searchInput.addEventListener("keydown", function (e) {
            if (e.key === "Enter") {
                clearTimeout(_debounceTimerRequest);
                buscarSolicitudes(this.value);
            }
        });
    }

    const form = document.getElementById("formSolicitud");
    if (form) {
        form.addEventListener("submit", function (e) {
            limpiarErroresSolicitud();
            let valido = true;

            //verificar que el cliente este cargado en los hidden
            const clienteIdVal = document.getElementById("clienteIdHidden")?.value;
            if (!clienteIdVal || clienteIdVal.trim() === "") {
                const errCliente = document.getElementById("errorCliente");
                if (errCliente) errCliente.style.display = "block";
                valido = false;
            }

            //detectar si es edicion para no bloquear fecha pasada
            const solicitudId = document.getElementById("solicitudId")?.value;
            const esEdicion = solicitudId && solicitudId.trim() !== "";

            const errF = validarFecha(document.getElementById("fecha").value, esEdicion);
            if (errF) { mostrarErrorSolicitud("errorFecha", errF); valido = false; }

            const errH = validarHora(document.getElementById("hora").value);
            if (errH) { mostrarErrorSolicitud("errorHora",  errH); valido = false; }

            if (!valido) e.preventDefault();
        });
    }

    document.getElementById("fecha")?.addEventListener("change", function () {
        limpiarErroresSolicitud();
        const solicitudId = document.getElementById("solicitudId")?.value;
        const esEdicion = solicitudId && solicitudId.trim() !== "";
        const err = validarFecha(this.value, esEdicion);
        if (err) mostrarErrorSolicitud("errorFecha", err);
    });

    document.getElementById("hora")?.addEventListener("change", function () {
        const err = validarHora(this.value);
        if (err) mostrarErrorSolicitud("errorHora", err);
        else {
            const el = document.getElementById("errorHora");
            if (el) { el.textContent = ""; el.style.display = "none"; }
        }
    });
});