//LOGICA DE FILTRO

let _debounceTimerRecord = null;

function alternarOrdenRecord() {
    const url = new URL(window.location.href);
    url.searchParams.set("orden", url.searchParams.get("orden") === "asc" ? "desc" : "asc");
    url.searchParams.set("page", 0);
    window.location.href = url.toString();
}

function cerrarFiltroRecord() {
    cerrarModal("filterRecordModal");
    setTimeout(() => volverPaso1Record(), 300);
}

function volverPaso1Record() {
    document.getElementById("filterRecordStep1").classList.remove("hidden");
    document.getElementById("filterRecordStep2").classList.add("hidden");
    document.getElementById("filterRecordTitle").textContent = "Filtrar por";
}

function mostrarPaso2Record(tipo) {
    document.getElementById("filterRecordStep1").classList.add("hidden");
    document.getElementById("filterRecordStep2").classList.remove("hidden");

    const titulos = {
        precio       : "Filtrar por precio",
        fecha        : "Filtrar por fecha",
        tipoServicio : "Filtrar por tipo de servicio"
    };
    document.getElementById("filterRecordTitle").textContent = titulos[tipo] || "Filtrar";

    const opciones = document.getElementById("filterRecordOpciones");
    opciones.innerHTML = "";

    if (tipo === "precio") {
        const asc  = document.createElement("button");
        const desc = document.createElement("button");
        asc.className  = desc.className = "filter-option";
        asc.textContent  = "↑ Menor a mayor";
        desc.textContent = "↓ Mayor a menor";
        asc.addEventListener("click",  () => aplicarFiltroRecord("orden", "asc"));
        desc.addEventListener("click", () => aplicarFiltroRecord("orden", "desc"));
        opciones.appendChild(asc);
        opciones.appendChild(desc);
    }

    if (tipo === "fecha") {
        const asc  = document.createElement("button");
        const desc = document.createElement("button");
        asc.className  = desc.className = "filter-option";
        asc.textContent  = "↑ Más próxima primero";
        desc.textContent = "↓ Más lejana primero";
        asc.addEventListener("click",  () => aplicarFiltroRecord("fecha", "asc"));
        desc.addEventListener("click", () => aplicarFiltroRecord("fecha", "desc"));
        opciones.appendChild(asc);
        opciones.appendChild(desc);
    }

    if (tipo === "tipoServicio") {
        if (!tiposServicioRecord || tiposServicioRecord.length === 0) {
            const msg = document.createElement("p");
            msg.className   = "filter-subtitle";
            msg.textContent = "No hay servicios disponibles.";
            opciones.appendChild(msg);
            return;
        }
        tiposServicioRecord.forEach(servicio => {
            const btn = document.createElement("button");
            btn.className   = "filter-option";
            btn.textContent = servicio.tipoServicio;
            btn.addEventListener("click", () => aplicarFiltroRecord("tipoServicio", servicio.tipoServicio));
            opciones.appendChild(btn);
        });
    }
}

function aplicarFiltroRecord(clave, valor) {
    cerrarFiltroRecord();
    const url = new URL(window.location.href);
    url.searchParams.delete("orden");
    url.searchParams.delete("fecha");
    url.searchParams.delete("tipoServicio");
    url.searchParams.set(clave, valor);
    url.searchParams.set("page", "0");
    window.location.href = url.toString();
}

function limpiarFiltroRecord() {
    cerrarFiltroRecord();
    const url = new URL(window.location.href);
    url.searchParams.delete("orden");
    url.searchParams.delete("fecha");
    url.searchParams.delete("tipoServicio");
    url.searchParams.set("page", "0");
    window.location.href = url.toString();
}

function buscarHistorial(termino) {
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

    document.getElementById("filterRecordBtn")
        ?.addEventListener("click", () => abrirModal("filterRecordModal"));

    document.getElementById("closeFilterRecord")
        ?.addEventListener("click", cerrarFiltroRecord);

    document.getElementById("filterRecordByPrecio")
        ?.addEventListener("click", () => mostrarPaso2Record("precio"));
    document.getElementById("filterRecordByFecha")
        ?.addEventListener("click", () => mostrarPaso2Record("fecha"));
    document.getElementById("filterRecordByTipoServicio")
        ?.addEventListener("click", () => mostrarPaso2Record("tipoServicio"));
    document.getElementById("filterRecordLimpiar")
        ?.addEventListener("click", limpiarFiltroRecord);
    document.getElementById("filterRecordBack")
        ?.addEventListener("click", volverPaso1Record);

    //busqueda en tiempo real con debounce
    const searchInput = document.getElementById("searchInput");
    if (searchInput) {
        const params = new URLSearchParams(window.location.search);
        if (params.get("busqueda")) searchInput.value = params.get("busqueda");

        searchInput.addEventListener("input", function () {
            const val = this.value;
            clearTimeout(_debounceTimerRecord);
            _debounceTimerRecord = setTimeout(() => buscarHistorial(val), 400);
        });

        searchInput.addEventListener("keydown", function (e) {
            if (e.key === "Enter") {
                clearTimeout(_debounceTimerRecord);
                buscarHistorial(this.value);
            }
        });
    }
});