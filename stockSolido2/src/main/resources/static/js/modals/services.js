//logica de modales y formulario de servicios

let _debounceTimerServices = null;

document.addEventListener("DOMContentLoaded", function () {

    document.getElementById("addServiceBtn")
        ?.addEventListener("click", abrirModalAgregarServicio);

    document.getElementById("filterServiceBtn")
        ?.addEventListener("click", alternarOrden);

    document.addEventListener("click", function (e) {
        if (e.target.matches(".action-btn.edit"))   abrirModalEditarServicio(e.target);
        if (e.target.matches(".action-btn.delete")) confirmarEliminar(e.target);
    });

    document.getElementById("closeModalServicio")
        ?.addEventListener("click", () => cerrarModal("modalServicio"));

    const form = document.getElementById("formServicio");
    if (form) {
        form.addEventListener("submit", function (e) {
            const id     = document.getElementById("servicioId").value.trim();
            const tipo   = document.getElementById("tipoServicio").value.trim();
            const precio = parseInt(document.getElementById("precioServicio").value);
            let valido   = true;

            document.getElementById("errorTipoServicio").textContent = "";
            document.getElementById("errorPrecioServicio").textContent = "";

            if (!id && tipo.length < 3) {
                document.getElementById("errorTipoServicio").textContent =
                    "El nombre debe tener al menos 3 caracteres.";
                valido = false;
            }

            if (!precio || precio <= 0) {
                document.getElementById("errorPrecioServicio").textContent =
                    "El precio debe ser mayor a 0.";
                valido = false;
            }

            if (!valido) e.preventDefault();
        });

        document.getElementById("precioServicio")?.addEventListener("input", function () {
            this.value = this.value.replace(/\D/g, "");
        });
    }

    //indicador de orden
    const url = new URL(window.location.href);
    const orden = url.searchParams.get('orden');
    const indicador = document.getElementById('ordenIndicador');
    if (indicador) {
        if (orden === 'asc')  indicador.textContent = '↑ Menor precio';
        if (orden === 'desc') indicador.textContent = '↓ Mayor precio';
    }

    //busqueda en tiempo real con debounce
    const searchInput = document.getElementById("searchInput");
    if (searchInput) {
        const params = new URLSearchParams(window.location.search);
        if (params.get("busqueda")) searchInput.value = params.get("busqueda");

        searchInput.addEventListener("input", function () {
            const val = this.value;
            clearTimeout(_debounceTimerServices);
            _debounceTimerServices = setTimeout(() => buscarServicios(val), 400);
        });

        searchInput.addEventListener("keydown", function (e) {
            if (e.key === "Enter") {
                clearTimeout(_debounceTimerServices);
                buscarServicios(this.value);
            }
        });
    }
});

function alternarOrden() {
    const url = new URL(window.location.href);
    const ordenActual = url.searchParams.get('orden');
    const nuevoOrden = ordenActual === 'asc' ? 'desc' : 'asc';
    url.searchParams.set('orden', nuevoOrden);
    url.searchParams.set('page', '0');
    window.location.href = url.toString();
}

function abrirModalAgregarServicio() {
    document.getElementById("servicioId").value = "";
    document.getElementById("modalTitleServicio").innerText = "Agregar Servicio";
    document.getElementById("btnGuardarServicio").innerText = "Guardar";
    document.getElementById("tipoServicio").value = "";
    document.getElementById("precioServicio").value = "";
    abrirModal("modalServicio");
}

function abrirModalEditarServicio(btn) {
    document.getElementById("servicioId").value = btn.dataset.id;
    document.getElementById("modalTitleServicio").innerText = "Editar Servicio";
    document.getElementById("btnGuardarServicio").innerText = "Actualizar";
    document.getElementById("tipoServicio").value = btn.dataset.tipoServicio;
    document.getElementById("precioServicio").value = btn.dataset.precioServicio;
    abrirModal("modalServicio");
}

function confirmarEliminar(btn) {
    const id = btn.dataset.id;
    const confirmBtn = document.getElementById("confirmDelete");
    const newBtn = confirmBtn.cloneNode(true);
    confirmBtn.parentNode.replaceChild(newBtn, confirmBtn);

    newBtn.addEventListener("click", function () {
        const csrfToken  = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

        fetch(`/private/admin/eliminarServicio/${id}`, {
            method: "DELETE",
            headers: { [csrfHeader]: csrfToken }
        })
        .then(res => {
            if (res.ok) { cerrarModal("deleteModal"); location.reload(); }
            else        { alert("Error al eliminar el servicio."); }
        })
        .catch(err => console.error("Error:", err));
    });

    abrirModal("deleteModal");
}

function buscarServicios(termino) {
    const url = new URL(window.location.href);
    if (termino && termino.trim()) {
        url.searchParams.set("busqueda", termino.trim());
    } else {
        url.searchParams.delete("busqueda");
    }
    url.searchParams.set("page", "0");
    window.location.href = url.toString();
}