    const API = "/private/admin/api";

    // Paleta de colores
    const COLORS = [
        "#cc0000","#1a1a1a","#e05555","#555555",
        "#f5a0a0","#888888","#ff6666","#333333"
    ];

    let chartInstances = {};

    async function apiFetch(endpoint) {
        const res = await fetch(`${API}/${endpoint}`, { credentials: "same-origin" });
        if (!res.ok) throw new Error(`Error ${res.status} en /${endpoint}`);
        return res.json();
    }

    function destroyChart(id) {
        if (chartInstances[id]) { chartInstances[id].destroy(); delete chartInstances[id]; }
    }
    function getCanvas(wrapperId) {
        const wrap = document.getElementById(wrapperId);
        wrap.innerHTML = "";
        const canvas = document.createElement("canvas");
        wrap.appendChild(canvas);
        return canvas;
    }
    function showError(wrapperId, msg) {
        document.getElementById(wrapperId).innerHTML =
            `<div class="error-state">⚠️ ${msg}</div>`;
    }
    function fmtMoneda(val) {
        const n = parseFloat(val) || 0;
        return "$" + n.toLocaleString("es-CO", { minimumFractionDigits: 0 });
    }
    function agruparPorMes(rows) {
        const mapa = {};
        for (const row of rows) {
            const fecha = row.fecha ? new Date(row.fecha) : null;
            const label = fecha
                ? fecha.toLocaleDateString("es-CO", { month: "short", year: "2-digit" })
                : "Sin fecha";
            mapa[label] = (mapa[label] || 0) + (parseFloat(row.total) || 0);
        }
        return mapa;
    }

    // Opciones base para Chart.js (fuente Montserrat)
    Chart.defaults.font.family = "'Montserrat', sans-serif";
    Chart.defaults.font.size   = 11;
    Chart.defaults.color       = "#777777";

    async function cargarIngresos() {
        try {
            const data  = await apiFetch("ingresos");
            const total = data.reduce((s, r) => s + (parseFloat(r.total) || 0), 0);
            document.getElementById("kpi-ingresos").textContent = fmtMoneda(total);

            const porMes = agruparPorMes(data);
            destroyChart("ingresosChart");
            const canvas = getCanvas("wrap-ingresos");
            chartInstances["ingresosChart"] = new Chart(canvas, {
                type: "bar",
                data: {
                    labels: Object.keys(porMes),
                    datasets: [{
                        label: "Ingresos ($)",
                        data: Object.values(porMes),
                        backgroundColor: "rgba(204,0,0,.15)",
                        borderColor: "#cc0000",
                        borderWidth: 2,
                        borderRadius: 6,
                        borderSkipped: false
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: { display: false },
                        tooltip: { callbacks: { label: ctx => " " + fmtMoneda(ctx.parsed.y) } }
                    },
                    scales: {
                        x: { grid: { display: false } },
                        y: {
                            beginAtZero: true,
                            grid: { color: "#f0f0f0" },
                            ticks: { callback: v => "$" + (v/1000).toFixed(0) + "k" }
                        }
                    }
                }
            });
        } catch (e) { showError("wrap-ingresos", e.message); }
    }

    async function cargarClientes() {
        try {
            const data = await apiFetch("clientes");
            document.getElementById("kpi-clientes").textContent = data.length;

            const porTipo = {};
            for (const c of data) { const t = c.tipo || "Sin tipo"; porTipo[t] = (porTipo[t] || 0) + 1; }

            destroyChart("clientesTipoChart");
            const canvas = getCanvas("wrap-clientes-tipo");
            chartInstances["clientesTipoChart"] = new Chart(canvas, {
                type: "bar",
                data: {
                    labels: Object.keys(porTipo),
                    datasets: [{
                        label: "Clientes",
                        data: Object.values(porTipo),
                        backgroundColor: COLORS.map(c => c + "33"),
                        borderColor: COLORS,
                        borderWidth: 2,
                        borderRadius: 6,
                        borderSkipped: false
                    }]
                },
                options: {
                    responsive: true,
                    plugins: { legend: { display: false } },
                    scales: {
                        x: { grid: { display: false } },
                        y: {
                            beginAtZero: true,
                            grid: { color: "#f0f0f0" },
                            ticks: { stepSize: 1 }
                        }
                    }
                }
            });
        } catch (e) { showError("wrap-clientes-tipo", e.message); }
    }

    async function cargarRanking() {
        try {
            const data = await apiFetch("ranking-servicios");
            document.getElementById("kpi-servicios").textContent = data.length;

            destroyChart("rankingChart");
            const canvas = getCanvas("wrap-ranking");
            chartInstances["rankingChart"] = new Chart(canvas, {
                type: "doughnut",
                data: {
                    labels: data.map(r => r.tipo_servicio || "—"),
                    datasets: [{
                        data: data.map(r => parseInt(r.cantidad) || 0),
                        backgroundColor: COLORS.slice(0, data.length),
                        borderWidth: 3,
                        borderColor: "#ffffff",
                        hoverOffset: 6
                    }]
                },
                options: {
                    responsive: true,
                    cutout: "62%",
                    plugins: {
                        legend: {
                            position: "bottom",
                            labels: { padding: 14, usePointStyle: true, pointStyleWidth: 8 }
                        }
                    }
                }
            });
        } catch (e) { showError("wrap-ranking", e.message); }
    }

    async function cargarPendientes() {
        try {
            const data = await apiFetch("pendientes");
            document.getElementById("kpi-pendientes").textContent = data.length;

            const porEstado = {};
            for (const s of data) { const e = s.estado || "Sin estado"; porEstado[e] = (porEstado[e] || 0) + 1; }

            destroyChart("estadosChart");
            const canvas = getCanvas("wrap-estados");
            chartInstances["estadosChart"] = new Chart(canvas, {
                type: "pie",
                data: {
                    labels: Object.keys(porEstado),
                    datasets: [{
                        data: Object.values(porEstado),
                        backgroundColor: ["#cc0000","#1a1a1a","#e05555","#888888"],
                        borderWidth: 3,
                        borderColor: "#ffffff",
                        hoverOffset: 6
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        legend: {
                            position: "bottom",
                            labels: { padding: 14, usePointStyle: true, pointStyleWidth: 8 }
                        }
                    }
                }
            });
        } catch (e) { showError("wrap-estados", e.message); }
    }

    async function cargarTodo() {
        await Promise.allSettled([
            cargarIngresos(),
            cargarClientes(),
            cargarRanking(),
            cargarPendientes()
        ]);
    }

    document.addEventListener("DOMContentLoaded", cargarTodo);