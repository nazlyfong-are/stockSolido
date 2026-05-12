/**
 * Muestra un modal identificado por su ID de elemento.
 * @param {string} id - ID del elemento modal en el DOM
 */
function abrirModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.style.display = "flex";
}

/**
 * Oculta un modal identificado por su ID de elemento.
 * @param {string} id - ID del elemento modal en el DOM
 */
function cerrarModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.style.display = "none";
}

/**
 * Capitaliza la primera letra de una cadena.
 * @param {string} str - cadena a capitalizar
 * @returns {string} cadena con la primera letra en mayusculas
 */
function capitalize(str) {
    if (!str) return "";
    return str.charAt(0).toUpperCase() + str.slice(1);
}

//inicializacion al cargar el DOM
document.addEventListener("DOMContentLoaded", function () {

    document.addEventListener("click", function (e) {
        const target = e.target.closest("[data-close]");
        if (target) {
            const modalId = target.getAttribute("data-close");
            cerrarModal(modalId);
        }
    });

    //modal de detalle de la fila
    const detailModal = document.getElementById("detailModal");
    const detailBody  = document.getElementById("detailBody");

    //etiquetas legibles para los campos data-* de las filas
    const etiquetas = {
        tipo        : "Tipo de Servicio",
        fecha       : "Fecha",
        hora        : "Hora",
        cliente     : "Cliente",
        documento   : "Documento",
        servicios   : "No. Servicios",
        total       : "Total",
        estado      : "Estado",
        descripcion : "Descripción",
        nombre      : "Nombre",
        correo      : "Correo",
        telefono    : "Teléfono"
    };

    //cerrar modal de detalle con el btn X
    document.getElementById("closeDetailModal")
        ?.addEventListener("click", () => cerrarModal("detailModal"));

    if (detailModal && detailBody) {
        document.addEventListener("click", function (e) {
            const fila = e.target.closest("tr");

            //ignorar clics fuera de filas, en el THEAD o en botones de accion
            if (!fila
                    || fila.parentElement.tagName === "THEAD"
                    || e.target.closest(".action-btn")
                    || e.target.closest("[data-close]")) {
                return;
            }

            //limpiar contenido anterior del modal
            detailBody.innerHTML = "";

            //construir filas del detalle desde los data-* de la fila
            Object.entries(fila.dataset).forEach(([clave, valor]) => {
                const etiqueta = etiquetas[clave] || capitalize(clave);
                const valorFormateado = clave === "total"
                    ? "$" + parseInt(valor).toLocaleString("es-CO")
                    : (valor || "—");

                detailBody.innerHTML += `
                    <div class="detail-row">
                        <span class="detail-label">${etiqueta}</span>
                        <span class="detail-value">${valorFormateado}</span>
                    </div>`;
            });

            detailModal.style.display = "flex";
        });
    }

    //cerrar el modal al hacer click fuera de el
    window.addEventListener("click", function (e) {
        document.querySelectorAll(".modal").forEach(modal => {
            if (e.target === modal) modal.style.display = "none";
        });
    });
});