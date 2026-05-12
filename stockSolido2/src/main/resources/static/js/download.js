const ENDPOINTS = {
    historial:  '/private/admin/reportes/historial',
    estado:     '/private/admin/reportes/estado',
    ingresos:   '/private/admin/reportes/ingresos',
    ranking:    '/private/admin/reportes/ranking',
    clientes:   '/private/admin/reportes/clientes',
    proximos:   '/private/admin/reportes/proximos',
    pendientes: '/private/admin/reportes/pendientes',
    cliente:    '/private/admin/reportes/cliente',
};

const TITULOS = {
    historial:  'Historial completo',
    estado:     'Solicitudes por estado',
    ingresos:   'Ingresos por período',
    ranking:    'Servicios más solicitados',
    clientes:   'Clientes registrados',
    proximos:   'Próximos servicios',
    pendientes: 'Servicios pendientes',
    cliente:    'Reporte por cliente',
};

//estado del modal
let _tipoActual = null;
let _blobUrl    = null;

/**cinstruye la URL con los parametros de filtro segun el tipo */
function buildUrl(tipo, preview = false) {
    const base   = ENDPOINTS[tipo] + (preview ? '/preview' : '');
    const params = new URLSearchParams();

    switch (tipo) {
        case 'estado': {
            const estado = document.getElementById('filtroEstado')?.value;
            if (estado) params.set('estado', estado);
            break;
        }
        case 'ingresos': {
            const fi = document.getElementById('fechaInicio')?.value;
            const ff = document.getElementById('fechaFin')?.value;
            if (fi) params.set('fechaInicio', fi);
            if (ff) params.set('fechaFin', ff);
            break;
        }
        case 'cliente': {
            const doc = document.getElementById('filtroDocumento')?.value?.trim();
            if (!doc) {
                alert('Por favor ingresa el número de documento del cliente.');
                return null;
            }
            params.set('documento', doc);
            break;
        }
    }

    const query = params.toString();
    return query ? `${base}?${query}` : base;
}

/**limpia el blob y el contenido del modal al cerrar */
function limpiarPreview() {
    if (_blobUrl) {
        URL.revokeObjectURL(_blobUrl);
        _blobUrl = null;
    }
    const body = document.getElementById('previewBody');
    if (body) body.innerHTML = '';
    _tipoActual = null;
    cerrarModal('previewModal');
}

/**
 * Descarga el PDF como Blob y lo muestra en un iframe dentro del modal.
 * Usar Blob URL evita el bloqueo del navegador con iframes en localhost.
 */
async function verPreviaPDF(tipo) {
    const url = buildUrl(tipo, true);
    if (!url) return;

    _tipoActual = tipo;
    document.getElementById('previewTitle').textContent = TITULOS[tipo] ?? 'Vista previa';

    const body = document.getElementById('previewBody');
    body.innerHTML = `
        <div style="display:flex;align-items:center;justify-content:center;
                    height:60vh;gap:12px;color:#636E72;font-family:Montserrat,sans-serif;">
            <svg width="24" height="24" fill="none" stroke="currentColor" stroke-width="2"
                 viewBox="0 0 24 24" style="animation:spin 1s linear infinite;flex-shrink:0">
                <path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83
                         M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/>
            </svg>
            Cargando vista previa…
        </div>
        <style>@keyframes spin{to{transform:rotate(360deg)}}</style>`;

    abrirModal('previewModal');

    try {
        const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content ?? '';
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content ?? 'X-CSRF-TOKEN';

        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                [csrfHeader]: csrfToken
            }
        });

        if (!response.ok) throw new Error(`Error ${response.status}: ${response.statusText}`);

        const blob = await response.blob();
        if (_blobUrl) URL.revokeObjectURL(_blobUrl);
        _blobUrl = URL.createObjectURL(blob);

        body.innerHTML = `
            <iframe
                src="${_blobUrl}"
                style="width:100%;height:70vh;border:none;border-radius:6px;"
                title="Vista previa PDF">
            </iframe>`;

    } catch (err) {
        body.innerHTML = `
            <div style="display:flex;flex-direction:column;align-items:center;justify-content:center;
                        height:60vh;gap:8px;color:#C0392B;font-family:Montserrat,sans-serif;">
                <svg width="32" height="32" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <circle cx="12" cy="12" r="10"/><path d="M12 8v4m0 4h.01"/>
                </svg>
                <strong>No se pudo cargar la vista previa</strong>
                <span style="font-size:13px;color:#636E72;">${err.message}</span>
            </div>`;
        console.error('[download.js] Error al cargar vista previa:', err);
    }
}

/**descarga directa sin abrir el modal */
function descargarDirecto(tipo) {
    const url = buildUrl(tipo, false);
    if (!url) return;

    const a = document.createElement('a');
    a.href = url;
    a.download = '';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
}

/**btn "Descargar PDF" dentro del modal de vista previa */
function descargarPDF() {
    if (_tipoActual) descargarDirecto(_tipoActual);
}

//inicializacion (DOMContentLoaded)
document.addEventListener('DOMContentLoaded', () => {

    //btns "Vista previa"  delegar por data-reporte
    document.addEventListener('click', (e) => {
        const btnPreview  = e.target.closest('.btn-preview[data-reporte]');
        const btnDownload = e.target.closest('.btn-download[data-reporte]');

        if (btnPreview)  verPreviaPDF(btnPreview.dataset.reporte);
        if (btnDownload) descargarDirecto(btnDownload.dataset.reporte);
    });

    //btn "Descargar PDF" dentro del modal de vista previa
    document.getElementById('btnDescargarPreview')
        ?.addEventListener('click', descargarPDF);

    //btn "Cerrar" y X del modal de vista previa
    document.getElementById('btnCerrarPreview')
        ?.addEventListener('click', limpiarPreview);
    document.getElementById('closePreviewModal')
        ?.addEventListener('click', limpiarPreview);

    //cerrar al hacer clic en el overlay del modal
    document.getElementById('previewModal')
        ?.addEventListener('click', (e) => {
            if (e.target === document.getElementById('previewModal')) limpiarPreview();
        });
});