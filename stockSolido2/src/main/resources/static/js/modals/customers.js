//LOGIC DE MODALES Y FORMULARIO DE CLIENTES

let _debounceTimerCustomers = null;

function mostrarErrorModal(mensaje) {
    let error = document.getElementById("errorModalCliente");
    if (!error) {
        error    = document.createElement("p");
        error.id = "errorModalCliente";
        error.className = "field-error field-error--modal";
        document.getElementById("formCliente")?.prepend(error);
    }
    error.textContent = mensaje;
}

function abrirModalAgregar() {
    document.getElementById("modalTitle").innerText    = "Agregar Cliente";
    document.getElementById("btnGuardar").innerText    = "Guardar";
    document.getElementById("tipo").value      = "Persona Natural";
    document.getElementById("documento").value = "";
    document.getElementById("nombre").value    = "";
    document.getElementById("correo").value    = "";
    document.getElementById("telefono").value  = "";
    const errEl = document.getElementById("errorModalCliente");
    if (errEl) errEl.textContent = "";
    abrirModal("modalCliente");
}

function abrirModalEditar(btn) {
    document.getElementById("modalTitle").innerText    = "Editar Cliente";
    document.getElementById("btnGuardar").innerText    = "Actualizar";
    document.getElementById("clienteId").value  = btn.dataset.id;
    document.getElementById("tipo").value       = btn.dataset.tipo;
    document.getElementById("documento").value  = btn.dataset.documento;
    document.getElementById("nombre").value     = btn.dataset.nombre;
    document.getElementById("correo").value     = btn.dataset.correo;
    document.getElementById("telefono").value   = btn.dataset.telefono;
    abrirModal("modalCliente");
}

function confirmarEliminar(btn) {
    const id = btn.dataset.id;
    document.getElementById("confirmDelete").onclick = function () {
        const csrfToken  = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
        fetch(`/private/admin/eliminarCliente/${id}`, {
            method: "DELETE",
            headers: { [csrfHeader]: csrfToken }
        })
        .then(res => {
            if (res.ok) { cerrarModal("deleteModal"); location.reload(); }
            else        { alert("Error al eliminar el cliente."); }
        })
        .catch(err => console.error("Error:", err));
    };
    abrirModal("deleteModal");
}

function aplicarFiltro(tipo) {
    cerrarModal("filterModal");
    const url = new URL(window.location.href);
    if (tipo) url.searchParams.set("tipo", tipo);
    else      url.searchParams.delete("tipo");
    url.searchParams.set("page", "0");
    window.location.href = url.toString();
}

function buscarClientes(termino) {
    const url = new URL(window.location.href);
    if (termino && termino.trim()) {
        url.searchParams.set("busqueda", termino.trim());
    } else {
        url.searchParams.delete("busqueda");
    }
    url.searchParams.set("page", "0");
    window.location.href = url.toString();
}

document.addEventListener("DOMContentLoaded", function () {

    document.getElementById("addClientBtn")
        ?.addEventListener("click", abrirModalAgregar);

    document.getElementById("filterClientBtn")
        ?.addEventListener("click", () => abrirModal("filterModal"));

    document.getElementById("closeModalCliente")
        ?.addEventListener("click", () => cerrarModal("modalCliente"));

    document.getElementById("closeFilterCliente")
        ?.addEventListener("click", () => cerrarModal("filterModal"));

    document.getElementById("filterPersonaNatural")
        ?.addEventListener("click", () => aplicarFiltro("Persona Natural"));
    document.getElementById("filterEmpresa")
        ?.addEventListener("click", () => aplicarFiltro("Empresa"));
    document.getElementById("filterTodos")
        ?.addEventListener("click", () => aplicarFiltro(""));

    document.addEventListener("click", function (e) {
        if (e.target.matches(".action-btn.edit"))   abrirModalEditar(e.target);
        if (e.target.matches(".action-btn.delete")) confirmarEliminar(e.target);
    });

    document.getElementById("tipo")
        ?.addEventListener("change", actualizarValidacionDocumento);

    const form = document.getElementById("formCliente");
    if (form) {
        form.addEventListener("submit", function (e) {
            const id        = document.getElementById("clienteId").value;
            const documento = document.getElementById("documento").value.trim();
            const tipo      = document.getElementById("tipo").value;

            if (id) return;

            if (!documento) {
                e.preventDefault();
                mostrarErrorModal("El número de documento es obligatorio.");
                return;
            }
            if (tipo === "Persona Natural" && !/^\d{6,10}$/.test(documento)) {
                e.preventDefault();
                mostrarErrorModal("La cédula debe tener entre 6 y 10 dígitos.");
                return;
            }
            if (tipo === "Empresa" && !/^\d{9,10}(-\d)?$/.test(documento)) {
                e.preventDefault();
                mostrarErrorModal("El NIT debe tener formato válido (ej: 900123456-1).");
                return;
            }
        });
    }

    //busqueda en tiempo real con debounce 
    const searchInput = document.getElementById("searchInput");
    if (searchInput) {
        const params = new URLSearchParams(window.location.search);
        if (params.get("busqueda")) searchInput.value = params.get("busqueda");

        searchInput.addEventListener("input", function () {
            const val = this.value;
            clearTimeout(_debounceTimerCustomers);
            _debounceTimerCustomers = setTimeout(() => buscarClientes(val), 400);
        });

        searchInput.addEventListener("keydown", function (e) {
            if (e.key === "Enter") {
                clearTimeout(_debounceTimerCustomers);
                buscarClientes(this.value);
            }
        });
    }
});

function actualizarValidacionDocumento() {
    const tipo = document.getElementById("tipo")?.value;
    const hint = document.getElementById("hintDocumento");
    if (!hint) return;
    hint.textContent = tipo === "Empresa"
        ? "NIT: 9–10 dígitos con guion opcional (ej: 900123456-1)"
        : "CC: 6–10 dígitos";
}