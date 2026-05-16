//logica de modales y formulario de solicitudes

let _debounceTimerRequest = null;

/**
 * Inicializa un desplegable con búsqueda.
 * @param {string} wrapperId   id del div .searchable-select
 * @param {string} searchId    id del input de búsqueda
 * @param {string} listId      id del <ul> de opciones
 * @param {string} displayId   id del div que muestra el valor seleccionado
 * @param {string} displayTextId  id del <span> de texto dentro del display
 * @param {string} noResultsId id del párrafo "sin resultados"
 * @param {Function} onSelect  callback(itemEl) cuando se elige una opción
 */
function initSearchableSelect({ wrapperId, searchId, listId, displayId, displayTextId, noResultsId, onSelect }) {
    const wrapper    = document.getElementById(wrapperId);
    const searchInput = document.getElementById(searchId);
    const list       = document.getElementById(listId);
    const display    = document.getElementById(displayId);
    const displayText = document.getElementById(displayTextId);
    const noResults  = document.getElementById(noResultsId);

    if (!wrapper || !searchInput || !list || !display) return;

    // Abrir / cerrar al hacer clic en el display
    display.addEventListener("click", (e) => {
        e.stopPropagation();
        const isOpen = wrapper.classList.contains("searchable-select--open");
        cerrarTodosLosSelectables();
        if (!isOpen) abrirSelectable(wrapper, searchInput);
    });

    display.addEventListener("keydown", (e) => {
        if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            const isOpen = wrapper.classList.contains("searchable-select--open");
            cerrarTodosLosSelectables();
            if (!isOpen) abrirSelectable(wrapper, searchInput);
        }
        if (e.key === "Escape") cerrarTodosLosSelectables();
    });

    // Filtrar al escribir
    searchInput.addEventListener("input", function () {
        const termino = this.value.toLowerCase().trim();
        let hayResultados = false;

        Array.from(list.querySelectorAll(".searchable-select__item")).forEach(item => {
            const texto = item.textContent.toLowerCase();
            const nombre    = (item.dataset.nombre    || "").toLowerCase();
            const documento = (item.dataset.documento || "").toLowerCase();
            const coincide = texto.includes(termino) || nombre.includes(termino) || documento.includes(termino);
            item.style.display = coincide ? "" : "none";
            if (coincide) hayResultados = true;
        });

        if (noResults) noResults.style.display = hayResultados ? "none" : "block";
    });

    // Seleccionar opcion
    list.addEventListener("click", (e) => {
        const item = e.target.closest(".searchable-select__item");
        if (!item) return;

        // Quitar seleccion previa
        list.querySelectorAll(".searchable-select__item--selected")
            .forEach(el => el.classList.remove("searchable-select__item--selected"));

        item.classList.add("searchable-select__item--selected");

        // Actualizar texto visible
        const esPlaceholder = item.classList.contains("searchable-select__empty");
        if (displayText) {
            displayText.textContent = esPlaceholder ? item.textContent : item.textContent.split(" — ")[0];
            displayText.className   = esPlaceholder
                ? "searchable-select__placeholder"
                : "searchable-select__selected-text";
        }

        cerrarTodosLosSelectables();
        if (onSelect) onSelect(item);
    });
}

function abrirSelectable(wrapper, input) {
    wrapper.classList.add("searchable-select--open");
    // Limpiar busquedda y mostrar todas las opciones
    if (input) {
        input.value = "";
        input.dispatchEvent(new Event("input"));
        setTimeout(() => input.focus(), 50);
    }
}

function cerrarTodosLosSelectables() {
    document.querySelectorAll(".searchable-select--open")
        .forEach(el => el.classList.remove("searchable-select--open"));
}

// Cerrar al hacer clic fuera
document.addEventListener("click", (e) => {
    if (!e.target.closest(".searchable-select")) {
        cerrarTodosLosSelectables();
    }
});

// helpers del formulario de solicitud

function seleccionarCliente(item) {
    document.getElementById("clienteIdHidden").value          = item.dataset.value  || "";
    document.getElementById("clienteNombreHidden").value      = item.dataset.nombre || "";
    document.getElementById("clienteDocumentoHidden").value   = item.dataset.documento || "";
    document.getElementById("clienteTipoHidden").value        = item.dataset.tipo   || "";
    document.getElementById("clienteCorreoHidden").value      = item.dataset.correo || "";
    document.getElementById("clienteTelefonoHidden").value    = item.dataset.telefono || "";

    // Ocultar error si estaba visible
    const err = document.getElementById("errorCliente");
    if (err) err.style.display = "none";
}

function seleccionarServicio(item) {
    const tipo   = item.dataset.value  || "";
    const precio = item.dataset.precio || "0";

    document.getElementById("tipoServicioHidden").value    = tipo;
    document.getElementById("servicioPrecioHidden").value  = precio;
    calcularTotal();
}

function calcularTotal() {
    const noServicios = parseInt(document.getElementById("noServicios").value) || 0;
    const precio      = parseFloat(document.getElementById("servicioPrecioHidden").value) || 0;
    const total       = precio * noServicios;

    document.getElementById("totalMostrado").value = "$" + total.toLocaleString("es-CO");
    document.getElementById("total").value         = total;
}

/**
 * Restaura visualmente el desplegable de cliente al valor de los hidden inputs.
 * Se usa al abrir el modal de edicion.
 */
function restaurarDisplayCliente(clienteId, clienteNombre) {
    const displayText = document.getElementById("clienteDisplayText");
    const list        = document.getElementById("clienteList");
    if (!displayText || !list) return;

    // Quitar selección previa
    list.querySelectorAll(".searchable-select__item--selected")
        .forEach(el => el.classList.remove("searchable-select__item--selected"));

    if (!clienteId) {
        displayText.textContent = "Seleccione un cliente";
        displayText.className   = "searchable-select__placeholder";
        return;
    }

    // Buscar el item por data-value
    const item = list.querySelector(`[data-value="${clienteId}"]`);
    if (item) {
        item.classList.add("searchable-select__item--selected");
        displayText.textContent = clienteNombre || item.textContent.split(" — ")[0];
        displayText.className   = "searchable-select__selected-text";
    } else {
        // Fallback: mostrar el nombre aunque no esté en la lista
        displayText.textContent = clienteNombre || "Cliente seleccionado";
        displayText.className   = "searchable-select__selected-text";
    }
}

/**
 * Restaura visualmente el desplegable de servicio.
 */
function restaurarDisplayServicio(tipoServicio) {
    const displayText = document.getElementById("servicioDisplayText");
    const list        = document.getElementById("servicioList");
    if (!displayText || !list) return;

    list.querySelectorAll(".searchable-select__item--selected")
        .forEach(el => el.classList.remove("searchable-select__item--selected"));

    if (!tipoServicio) {
        displayText.textContent = "Seleccione un servicio";
        displayText.className   = "searchable-select__placeholder";
        return;
    }

    const item = list.querySelector(`[data-value="${tipoServicio}"]`);
    if (item) {
        item.classList.add("searchable-select__item--selected");
        displayText.textContent = tipoServicio;
        displayText.className   = "searchable-select__selected-text";
    }
}

//abrir modales

function limpiarHiddenCliente() {
    ["clienteIdHidden","clienteNombreHidden","clienteDocumentoHidden",
     "clienteTipoHidden","clienteCorreoHidden","clienteTelefonoHidden"]
        .forEach(id => { const el = document.getElementById(id); if (el) el.value = ""; });
}

function abrirModalAgregarSolicitud() {
    document.getElementById("modalTitleSolicitud").innerText = "Agregar Solicitud";
    document.getElementById("btnGuardarSolicitud").innerText = "Guardar";
    document.getElementById("solicitudId").value   = "";
    document.getElementById("fecha").value         = "";
    document.getElementById("hora").value          = "";
    document.getElementById("noServicios").value   = "";
    document.getElementById("descripcion").value   = "";
    document.getElementById("estado").value        = "En espera";
    document.getElementById("contadorDescripcion").innerText = "0 / 150";
    document.getElementById("totalMostrado").value = "";
    document.getElementById("total").value         = "";
    document.getElementById("tipoServicioHidden").value    = "";
    document.getElementById("servicioPrecioHidden").value  = "";

    limpiarHiddenCliente();
    restaurarDisplayCliente("", "");
    restaurarDisplayServicio("");

    const err = document.getElementById("errorCliente");
    if (err) err.style.display = "none";

    limpiarErroresSolicitud();
    cerrarTodosLosSelectables();
    abrirModal("modalSolicitud");
}

function abrirModalEditarSolicitud(btn) {
    document.getElementById("modalTitleSolicitud").innerText = "Editar Solicitud";
    document.getElementById("btnGuardarSolicitud").innerText = "Actualizar";
    document.getElementById("solicitudId").value             = btn.dataset.id;

    // Cargar hidden inputs del cliente
    document.getElementById("clienteIdHidden").value         = btn.dataset.clienteId       || "";
    document.getElementById("clienteNombreHidden").value     = btn.dataset.clienteNombre   || "";
    document.getElementById("clienteDocumentoHidden").value  = btn.dataset.clienteDocumento|| "";
    document.getElementById("clienteTipoHidden").value       = btn.dataset.clienteTipo     || "";
    document.getElementById("clienteCorreoHidden").value     = btn.dataset.clienteCorreo   || "";
    document.getElementById("clienteTelefonoHidden").value   = btn.dataset.clienteTelefono || "";

    // Cargar servicio
    document.getElementById("tipoServicioHidden").value      = btn.dataset.tipoServicio    || "";
    document.getElementById("servicioPrecioHidden").value    = btn.dataset.servicioPrecio  || "0";

    // Otros campos
    document.getElementById("noServicios").value  = btn.dataset.noServicios;
    document.getElementById("fecha").value        = btn.dataset.fecha;
    document.getElementById("hora").value         = btn.dataset.hora;
    document.getElementById("descripcion").value  = btn.dataset.descripcion;
    document.getElementById("estado").value       = btn.dataset.estado;
    document.getElementById("contadorDescripcion").innerText =
        `${(btn.dataset.descripcion || "").length} / 150`;

    // Restaurar displays visuales
    restaurarDisplayCliente(btn.dataset.clienteId, btn.dataset.clienteNombre);
    restaurarDisplayServicio(btn.dataset.tipoServicio);

    const err = document.getElementById("errorCliente");
    if (err) err.style.display = "none";

    calcularTotal();
    limpiarErroresSolicitud();
    cerrarTodosLosSelectables();
    abrirModal("modalSolicitud");
}

function confirmarEliminar(btn) {
    const id = btn.dataset.id;
    document.getElementById("confirmDelete").onclick = function () {
        const csrfToken  = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        fetch(`/private/admin/eliminarSolicitud/${id}`, {
            method: "DELETE",
            headers: { [csrfHeader]: csrfToken }
        })
        .then(res => {
            if (res.ok) { cerrarModal("deleteModal"); location.reload(); }
            else        { alert("Error al eliminar la solicitud."); }
        })
        .catch(err => console.error("Error:", err));
    };
    abrirModal("deleteModal");
}

//filtro
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
        placeholder.value       = "";
        placeholder.textContent = "Seleccione un servicio...";
        placeholder.disabled    = true;
        placeholder.selected    = true;
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
            if (select.value) aplicarFiltroRequest("tipoServicio", select.value);
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

//busqueda global

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

//validacion

function validarFecha(fecha) {
    if (!fecha) return "La fecha es obligatoria.";
    const hoy = new Date(); hoy.setHours(0, 0, 0, 0);
    const sel = new Date(fecha + "T00:00:00");
    if (sel < hoy)           return "No se pueden agendar solicitudes en fechas pasadas.";
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

//init
document.addEventListener("DOMContentLoaded", function () {

    // Inicializar desplegables con busqueda
    initSearchableSelect({
        wrapperId    : "clienteSelectWrapper",
        searchId     : "clienteSearchInput",
        listId       : "clienteList",
        displayId    : "clienteDisplay",
        displayTextId: "clienteDisplayText",
        noResultsId  : "clienteNoResults",
        onSelect     : seleccionarCliente
    });

    initSearchableSelect({
        wrapperId    : "servicioSelectWrapper",
        searchId     : "servicioSearchInput",
        listId       : "servicioList",
        displayId    : "servicioDisplay",
        displayTextId: "servicioDisplayText",
        noResultsId  : "servicioNoResults",
        onSelect     : seleccionarServicio
    });

    // Botones de la pagina
    document.getElementById("addRequestBtn")
        ?.addEventListener("click", abrirModalAgregarSolicitud);

    document.getElementById("filterRequestBtn")
        ?.addEventListener("click", () => abrirModal("filterRequestModal"));

    document.getElementById("closeModalSolicitud")
        ?.addEventListener("click", () => {
            cerrarTodosLosSelectables();
            cerrarModal("modalSolicitud");
        });

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

    document.getElementById("noServicios")
        ?.addEventListener("input", calcularTotal);

    document.getElementById("descripcion")
        ?.addEventListener("input", function () {
            document.getElementById("contadorDescripcion").innerText = `${this.value.length} / 150`;
        });

    // busqueda global
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

    // validacion al enviar
    const form = document.getElementById("formSolicitud");
    if (form) {
        form.addEventListener("submit", function (e) {
            limpiarErroresSolicitud();
            let valido = true;

            // Verificar que se selecciono un cliente
            const clienteId = document.getElementById("clienteIdHidden")?.value;
            if (!clienteId || clienteId.trim() === "") {
                const err = document.getElementById("errorCliente");
                if (err) err.style.display = "block";
                valido = false;
            }

            const errF = validarFecha(document.getElementById("fecha").value);
            if (errF) { mostrarErrorSolicitud("errorFecha", errF); valido = false; }

            const errH = validarHora(document.getElementById("hora").value);
            if (errH) { mostrarErrorSolicitud("errorHora",  errH); valido = false; }

            if (!valido) e.preventDefault();
        });
    }

    document.getElementById("fecha")?.addEventListener("change", function () {
        limpiarErroresSolicitud();
        const err = validarFecha(this.value);
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