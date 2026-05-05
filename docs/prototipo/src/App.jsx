import { useState } from "react";

// ─── DESIGN TOKENS ───────────────────────────────────────────────────────────
const C = {
  color_fondo: "#0F1117",
  color_superficie: "#171B26",
  color_superficie_hover: "#1E2436",
  color_borde: "#252D40",
  color_borde_claro: "#2E3750",
  color_acento: "#3B82F6",
  color_acento_oscuro: "#1D4ED8",
  color_acento_brillo: "rgba(59,130,246,0.15)",
  color_exito: "#10B981",
  color_exito_tenue: "rgba(16,185,129,0.12)",
  color_alerta: "#F59E0B",
  color_alerta_tenue: "rgba(245,158,11,0.12)",
  color_peligro: "#EF4444",
  color_peligro_tenue: "rgba(239,68,68,0.12)",
  color_texto: "#E2E8F0",
  color_texto_atenuado: "#64748B",
  color_texto_secundario: "#94A3B8",
  color_blanco: "#FFFFFF",
};

const styles = `
  @import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@300;400;500;600;700&family=DM+Mono:wght@400;500&display=swap');
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body { font-family: 'DM Sans', sans-serif; background: ${C.color_fondo}; color: ${C.color_texto}; }
  ::-webkit-scrollbar { width: 5px; }
  ::-webkit-scrollbar-track { background: ${C.color_fondo}; }
  ::-webkit-scrollbar-thumb { background: ${C.color_borde}; border-radius: 3px; }

  .layout { display: flex; height: 100vh; overflow: hidden; }

  /* SIDEBAR */
  .sidebar { width: 256px; min-width: 256px; background: ${C.color_superficie}; border-right: 1px solid ${C.color_borde}; display: flex; flex-direction: column; overflow-y: auto; }
  .sidebar-logo { padding: 20px 20px 16px; border-bottom: 1px solid ${C.color_borde}; }
  .logo-badge { background: ${C.color_acento}; color: #fff; font-size: 10px; font-weight: 700; letter-spacing: 1px; padding: 3px 8px; border-radius: 4px; display: inline-block; margin-bottom: 6px; }
  .logo-title { font-size: 14px; font-weight: 600; color: ${C.color_texto}; line-height: 1.3; }
  .logo-sub { font-size: 11px; color: ${C.color_texto_atenuado}; margin-top: 2px; }
  .sidebar-section { padding: 16px 12px 8px; }
  .sidebar-section-label { font-size: 10px; font-weight: 600; letter-spacing: 1.5px; color: ${C.color_texto_atenuado}; text-transform: uppercase; padding: 0 8px; margin-bottom: 6px; }
  .nav-item { display: flex; align-items: center; gap: 10px; padding: 9px 10px; border-radius: 8px; cursor: pointer; font-size: 13px; color: ${C.color_texto_secundario}; transition: all 0.15s; border: 1px solid transparent; margin-bottom: 2px; }
  .nav-item:hover { background: ${C.color_superficie_hover}; color: ${C.color_texto}; }
  .nav-item.active { background: ${C.color_acento_brillo}; color: ${C.color_acento}; border-color: rgba(59,130,246,0.2); }
  .nav-icon { font-size: 15px; width: 20px; text-align: center; flex-shrink: 0; }
  .nav-badge { margin-left: auto; background: ${C.color_peligro}; color: #fff; font-size: 10px; font-weight: 700; padding: 1px 6px; border-radius: 10px; }
  .nav-badge-warn { background: ${C.color_alerta}; }

  /* MAIN */
  .main { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
  .topbar { height: 56px; min-height: 56px; background: ${C.color_superficie}; border-bottom: 1px solid ${C.color_borde}; display: flex; align-items: center; padding: 0 24px; gap: 16px; }
  .topbar-title { font-size: 15px; font-weight: 600; color: ${C.color_texto}; }
  .topbar-sub { font-size: 12px; color: ${C.color_texto_atenuado}; margin-left: 4px; }
  .topbar-right { margin-left: auto; display: flex; align-items: center; gap: 12px; }
  .avatar { width: 32px; height: 32px; border-radius: 50%; background: ${C.color_acento_brillo}; border: 1px solid rgba(59,130,246,0.3); display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; color: ${C.color_acento}; }
  .content { flex: 1; overflow-y: auto; padding: 24px; }

  /* CARDS & PANELS */
  .card { background: ${C.color_superficie}; border: 1px solid ${C.color_borde}; border-radius: 12px; padding: 20px; margin-bottom: 16px; }
  .card-title { font-size: 14px; font-weight: 600; color: ${C.color_texto}; margin-bottom: 16px; display: flex; align-items: center; gap: 8px; }
  .card-title span { font-size: 16px; }
  .grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
  .grid-3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px; }
  .grid-4 { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }

  /* STAT CARDS */
  .stat-card { background: ${C.color_superficie}; border: 1px solid ${C.color_borde}; border-radius: 12px; padding: 18px 20px; }
  .stat-label { font-size: 11px; color: ${C.color_texto_atenuado}; font-weight: 500; letter-spacing: 0.5px; margin-bottom: 8px; text-transform: uppercase; }
  .stat-value { font-size: 28px; font-weight: 700; color: ${C.color_texto}; font-family: 'DM Mono', monospace; line-height: 1; }
  .stat-value.green { color: ${C.color_exito}; }
  .stat-value.amber { color: ${C.color_alerta}; }
  .stat-value.red { color: ${C.color_peligro}; }
  .stat-sub { font-size: 11px; color: ${C.color_texto_atenuado}; margin-top: 6px; }

  /* TABLES */
  .table-wrap { overflow-x: auto; }
  table { width: 100%; border-collapse: collapse; font-size: 13px; }
  thead tr { border-bottom: 1px solid ${C.color_borde}; }
  th { padding: 10px 14px; text-align: left; font-size: 11px; font-weight: 600; color: ${C.color_texto_atenuado}; letter-spacing: 0.5px; text-transform: uppercase; }
  td { padding: 12px 14px; color: ${C.color_texto_secundario}; border-bottom: 1px solid rgba(37,45,64,0.5); }
  tr:last-child td { border-bottom: none; }
  tr:hover td { background: ${C.color_superficie_hover}; }
  .td-main { color: ${C.color_texto}; font-weight: 500; }
  .mono { font-family: 'DM Mono', monospace; font-size: 12px; }

  /* BADGES */
  .badge { display: inline-flex; align-items: center; gap: 4px; padding: 3px 8px; border-radius: 20px; font-size: 11px; font-weight: 600; }
  .badge-green { background: ${C.color_exito_tenue}; color: ${C.color_exito}; }
  .badge-amber { background: ${C.color_alerta_tenue}; color: ${C.color_alerta}; }
  .badge-red { background: ${C.color_peligro_tenue}; color: ${C.color_peligro}; }
  .badge-blue { background: ${C.color_acento_brillo}; color: ${C.color_acento}; }
  .badge-gray { background: rgba(100,116,139,0.12); color: ${C.color_texto_atenuado}; }
  .badge-purple { background: rgba(139,92,246,0.12); color: #8B5CF6; }

  /* FORM ELEMENTS */
  .form-row { margin-bottom: 16px; }
  .form-label { font-size: 12px; font-weight: 500; color: ${C.color_texto_secundario}; margin-bottom: 6px; display: block; }
  .form-input { width: 100%; background: ${C.color_fondo}; border: 1px solid ${C.color_borde}; border-radius: 8px; padding: 9px 12px; font-size: 13px; color: ${C.color_texto}; font-family: 'DM Sans', sans-serif; outline: none; transition: border 0.15s; }
  .form-input:focus { border-color: ${C.color_acento}; }
  .form-input::placeholder { color: ${C.color_texto_atenuado}; }
  .form-select { width: 100%; background: ${C.color_fondo}; border: 1px solid ${C.color_borde}; border-radius: 8px; padding: 9px 12px; font-size: 13px; color: ${C.color_texto}; font-family: 'DM Sans', sans-serif; outline: none; cursor: pointer; }
  .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
  .form-grid-3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px; }
  .hint { font-size: 11px; color: ${C.color_texto_atenuado}; margin-top: 4px; }
  .required { color: ${C.color_peligro}; margin-left: 2px; }

  /* BUTTONS */
  .btn { display: inline-flex; align-items: center; gap: 6px; padding: 9px 16px; border-radius: 8px; font-size: 13px; font-weight: 600; cursor: pointer; border: none; font-family: 'DM Sans', sans-serif; transition: all 0.15s; }
  .btn-primary { background: ${C.color_acento}; color: #fff; }
  .btn-primary:hover { background: ${C.color_acento_oscuro}; }
  .btn-secondary { background: ${C.color_superficie_hover}; color: ${C.color_texto_secundario}; border: 1px solid ${C.color_borde}; }
  .btn-secondary:hover { color: ${C.color_texto}; border-color: ${C.color_borde_claro}; }
  .btn-danger { background: ${C.color_peligro_tenue}; color: ${C.color_peligro}; border: 1px solid rgba(239,68,68,0.2); }
  .btn-success { background: ${C.color_exito_tenue}; color: ${C.color_exito}; border: 1px solid rgba(16,185,129,0.2); }
  .btn-sm { padding: 6px 12px; font-size: 12px; }
  .btn-row { display: flex; gap: 10px; margin-top: 20px; }

  /* ALERTS */
  .alert { padding: 12px 16px; border-radius: 8px; font-size: 13px; margin-bottom: 16px; display: flex; align-items: flex-start; gap: 10px; }
  .alert-warn { background: ${C.color_alerta_tenue}; border: 1px solid rgba(245,158,11,0.2); color: ${C.color_alerta}; }
  .alert-danger { background: ${C.color_peligro_tenue}; border: 1px solid rgba(239,68,68,0.2); color: ${C.color_peligro}; }
  .alert-success { background: ${C.color_exito_tenue}; border: 1px solid rgba(16,185,129,0.2); color: ${C.color_exito}; }
  .alert-info { background: ${C.color_acento_brillo}; border: 1px solid rgba(59,130,246,0.2); color: ${C.color_acento}; }

  /* STEP INDICATOR */
  .steps { display: flex; align-items: center; gap: 0; margin-bottom: 24px; }
  .step { display: flex; align-items: center; }
  .step-circle { width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; flex-shrink: 0; }
  .step-circle.done { background: ${C.color_exito}; color: #fff; }
  .step-circle.active { background: ${C.color_acento}; color: #fff; }
  .step-circle.pending { background: ${C.color_borde}; color: ${C.color_texto_atenuado}; }
  .step-label { font-size: 12px; color: ${C.color_texto_atenuado}; margin-left: 8px; white-space: nowrap; }
  .step-label.active { color: ${C.color_texto}; font-weight: 500; }
  .step-line { width: 40px; height: 1px; background: ${C.color_borde}; margin: 0 8px; flex-shrink: 0; }
  .step-line.done { background: ${C.color_exito}; }

  /* FEFO BAR */
  .fefo-bar { height: 4px; border-radius: 2px; margin-top: 6px; }
  .progress-bar-wrap { background: ${C.color_borde}; border-radius: 4px; height: 6px; overflow: hidden; }
  .progress-bar-fill { height: 100%; border-radius: 4px; transition: width 0.3s; }

  /* MISC */
  .divider { border: none; border-top: 1px solid ${C.color_borde}; margin: 20px 0; }
  .empty-state { text-align: center; padding: 48px 24px; color: ${C.color_texto_atenuado}; }
  .empty-state .icon { font-size: 36px; margin-bottom: 12px; }
  .empty-state p { font-size: 13px; }
  .page-header { margin-bottom: 24px; }
  .page-header h1 { font-size: 20px; font-weight: 700; color: ${C.color_texto}; }
  .page-header p { font-size: 13px; color: ${C.color_texto_atenuado}; margin-top: 4px; }
  .search-bar { background: ${C.color_fondo}; border: 1px solid ${C.color_borde}; border-radius: 8px; padding: 8px 12px; font-size: 13px; color: ${C.color_texto}; font-family: 'DM Sans', sans-serif; outline: none; width: 240px; }
  .search-bar:focus { border-color: ${C.color_acento}; }
  .toolbar { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
  .toolbar-right { margin-left: auto; display: flex; gap: 8px; }
  .lote-row-critical { background: rgba(245,158,11,0.05) !important; }
  .dot { width: 7px; height: 7px; border-radius: 50%; display: inline-block; margin-right: 4px; }
  .dot-green { background: ${C.color_exito}; }
  .dot-amber { background: ${C.color_alerta}; }
  .dot-red { background: ${C.color_peligro}; }
  .dot-blue { background: ${C.color_acento}; }
  .detail-row { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid rgba(37,45,64,0.5); font-size: 13px; }
  .detail-row:last-child { border-bottom: none; }
  .detail-key { color: ${C.color_texto_atenuado}; }
  .detail-val { color: ${C.color_texto}; font-weight: 500; }
  .tag-list { display: flex; gap: 6px; flex-wrap: wrap; }
  .modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.7); z-index: 100; display: flex; align-items: center; justify-content: center; }
  .modal { background: ${C.color_superficie}; border: 1px solid ${C.color_borde}; border-radius: 16px; padding: 28px; width: 480px; max-width: 90vw; max-height: 85vh; overflow-y: auto; }
  .modal-title { font-size: 16px; font-weight: 700; color: ${C.color_texto}; margin-bottom: 20px; }
`;

// ─── DATA ────────────────────────────────────────────────────────────────────
const productos = [
  { sku: "SKU-001", marca: "Águila", presentacion: "Six-pack", contenido: 330, peso: 2.40, stock: 240, comprometido: 60 },
  { sku: "SKU-002", marca: "Club Colombia", presentacion: "Caja", contenido: 355, peso: 8.60, stock: 180, comprometido: 30 },
  { sku: "SKU-003", marca: "Poker", presentacion: "Barril", contenido: 30000, peso: 30.00, stock: 0, comprometido: 0 },
  { sku: "SKU-004", marca: "Heineken", presentacion: "Six-pack", contenido: 330, peso: 2.50, stock: 96, comprometido: 24 },
  { sku: "SKU-005", marca: "Costeña", presentacion: "Estiba", contenido: 330, peso: 120.00, stock: 312, comprometido: 0 },
];

const lotes = [
  { codigo: "LOT-2024-001", sku: "SKU-001", marca: "Águila", vencimiento: "2024-04-15", cantidad: 120, estado: "Disponible", critico: true, costo: 2800 },
  { codigo: "LOT-2024-002", sku: "SKU-001", marca: "Águila", vencimiento: "2024-08-20", cantidad: 120, estado: "Disponible", critico: false, costo: 2800 },
  { codigo: "LOT-2024-003", sku: "SKU-002", marca: "Club Colombia", vencimiento: "2024-09-10", cantidad: 180, estado: "Disponible", critico: false, costo: 3500 },
  { codigo: "LOT-2024-004", sku: "SKU-004", marca: "Heineken", vencimiento: "2024-05-01", cantidad: 48, estado: "Disponible", critico: true, costo: 4200 },
  { codigo: "LOT-2023-005", sku: "SKU-001", marca: "Águila", vencimiento: "2024-01-10", cantidad: 24, estado: "Vencido", critico: false, costo: 2800 },
  { codigo: "LOT-2024-006", sku: "SKU-005", marca: "Costeña", vencimiento: "2025-02-15", cantidad: 312, estado: "Disponible", critico: false, costo: 2500 },
];

const pedidos = [
  { numero: "PED-2024-001", cliente: "Supermercado El Rey", nit: "800.123.456-1", fecha: "2026-03-08", estado: "Comprometido", productos: [{ sku: "SKU-001", nombre: "Águila Six-pack", cantidad: 48, lote: "LOT-2024-002" }, { sku: "SKU-002", nombre: "Club Colombia Caja", cantidad: 24, lote: "LOT-2024-003" }], total: 218400 },
  { numero: "PED-2024-002", cliente: "Tienda La Esquina", nit: "900.456.789-2", fecha: "2026-03-09", estado: "Comprometido", productos: [{ sku: "SKU-004", nombre: "Heineken Six-pack", cantidad: 12, lote: "LOT-2024-004" }], total: 50400 },
  { numero: "PED-2024-003", cliente: "Bar Rincón Paisa", nit: "700.111.222-3", fecha: "2026-03-10", estado: "En Picking", productos: [{ sku: "SKU-001", nombre: "Águila Six-pack", cantidad: 24, lote: "LOT-2024-001" }], total: 67200 },
  { numero: "PED-2024-004", cliente: "Almacén del Norte", nit: "860.555.333-4", fecha: "2026-03-07", estado: "Esperando Ruta", productos: [{ sku: "SKU-005", nombre: "Costeña Estiba", cantidad: 60, lote: "LOT-2024-006" }], total: 150000 },
  { numero: "PED-2024-005", cliente: "Supermercado El Rey", nit: "800.123.456-1", fecha: "2026-03-06", estado: "Despachado", productos: [{ sku: "SKU-002", nombre: "Club Colombia Caja", cantidad: 48, lote: "LOT-2024-003" }], total: 168000 },
];



const excepciones = [
  { id: "EXC-001", tipo: "Avería", lote: "LOT-2024-001", cantidad: 6, origen: "Inspección", fecha: "2026-03-10", usuario: "Supervisor", estado: "Registrada" },
  { id: "EXC-002", tipo: "Vencimiento", lote: "LOT-2023-005", cantidad: 24, origen: "Proceso automático", fecha: "2026-03-09", usuario: "Sistema", estado: "Registrada" },
  { id: "EXC-003", tipo: "Diferencia de Inventario", lote: "LOT-2024-003", cantidad: 12, origen: "Recepción", fecha: "2026-03-08", usuario: "Ana Martínez", estado: "Registrada" },
];

// ─── NAV CONFIG ───────────────────────────────────────────────────────────────
const nav = [
  {
    section: "SUPERVISOR DE INVENTARIO", items: [
      { id: "dashboard", icon: "◈", label: "Dashboard" },
      { id: "crear-plantilla", icon: "＋", label: "Crear Plantilla" },
      { id: "modificar-plantilla", icon: "✎", label: "Modificar Plantilla" },
      { id: "consultar-inventario", icon: "◫", label: "Consultar Inventario" },
      { id: "excepciones", icon: "⚠", label: "Excepciones", badge: "3" },
      { id: "reportar-excepcion", icon: "!", label: "Reportar Excepción" },
    ]
  },
  {
    section: "OPERARIO DE RECEPCIÓN", items: [
      { id: "listar-manifiestos", icon: "📋", label: "Listar Manifiestos" },
      { id: "registrar-ingreso", icon: "↓", label: "Registrar Ingreso" },
    ]
  },
  {
    section: "OPERARIO DE PICKING/DESPACHO", items: [
      { id: "listar-pedidos", icon: "≡", label: "Pedidos Comprometidos", badge: "2", badgeClass: "nav-badge" },
      { id: "confirmar-picking", icon: "✓", label: "Confirmar Picking" },
      { id: "confirmar-despacho", icon: "⬆", label: "Confirmar Despacho" },
    ]
  },
  {
    section: "ASESOR COMERCIAL", items: [
      { id: "consultar-cliente", icon: "👤", label: "Consultar Cliente CC" },
      { id: "catalogo", icon: "◉", label: "Catálogo de Productos" },
      { id: "realizar-pedido", icon: "🛒", label: "Realizar Pedido" },
    ]
  },
  {
    section: "INTEGRACIONES", items: [
      { id: "consultar-detalle-pedido", icon: "⊡", label: "Consultar Detalle Pedido" },
    ]
  },
];

// ─── HELPER COMPONENTS ────────────────────────────────────────────────────────
const Badge = ({ type, children }) => {
  const cls = { green: "badge-green", amber: "badge-amber", red: "badge-red", blue: "badge-blue", gray: "badge-gray", purple: "badge-purple" }[type] || "badge-gray";
  return <span className={`badge ${cls}`}>{children}</span>;
};

const estadoBadge = (estado) => {
  const map = {
    "Disponible": ["green", "Disponible"],
    "Comprometido": ["blue", "Comprometido"],
    "En Picking": ["amber", "En Picking"],
    "Picking": ["amber", "En Picking"],
    "Despachado": ["purple", "Despachado"],
    "Entregado": ["green", "Entregado"],
    "Esperando Ruta": ["gray", "Esperando Ruta"],
    "Vencido": ["red", "Vencido"],
    "Avería": ["red", "Avería"],
    "Con excepción": ["amber", "Con excepción"],
    "Completada": ["green", "Completada"],
    "Registrada": ["blue", "Registrada"],
  };
  const [type, label] = map[estado] || ["gray", estado];
  return <Badge type={type}>{label}</Badge>;
};

// ─── SCREENS ─────────────────────────────────────────────────────────────────

function Dashboard() {
  return (
    <div>
      <div className="page-header">
        <h1>Dashboard — Módulo 1</h1>
        <p>Vista general del inventario y operaciones del día</p>
      </div>
      <div className="grid-4" style={{ marginBottom: 16 }}>
        <div className="stat-card"><div className="stat-label">SKUs en catálogo</div><div className="stat-value">5</div><div className="stat-sub">4 con stock disponible</div></div>
        <div className="stat-card"><div className="stat-label">Lotes disponibles</div><div className="stat-value green">6</div><div className="stat-sub">2 con alerta FEFO</div></div>
        <div className="stat-card"><div className="stat-label">Pedidos comprometidos</div><div className="stat-value amber">2</div><div className="stat-sub">Pendientes de picking</div></div>
        <div className="stat-card"><div className="stat-label">Excepciones hoy</div><div className="stat-value red">3</div><div className="stat-sub">Requieren atención</div></div>
      </div>
      <div className="grid-2">
        <div className="card">
          <div className="card-title"><span>⚠</span> Alertas FEFO Críticas</div>
          <div className="alert alert-warn" style={{ marginBottom: 10 }}>
            <span>⚠</span>
            <div><strong>LOT-2024-001</strong> — Águila 330ml vence en <strong>5 días</strong> (15/04/2024)</div>
          </div>
          <div className="alert alert-warn">
            <span>⚠</span>
            <div><strong>LOT-2024-004</strong> — Heineken 330ml vence en <strong>21 días</strong> (01/05/2024)</div>
          </div>
        </div>
        <div className="card">
          <div className="card-title"><span>◈</span> Estado de Pedidos</div>
          {[["Esperando Ruta", 1, "gray"], ["Comprometidos", 2, "blue"], ["En Picking", 1, "amber"], ["Despachados hoy", 1, "purple"]].map(([label, n, type]) => (
            <div key={label} className="detail-row">
              <span className="detail-key">{label}</span>
              <Badge type={type}>{n}</Badge>
            </div>
          ))}
        </div>
      </div>
      <div className="card">
        <div className="card-title"><span>◫</span> Stock por Producto</div>
        <div className="table-wrap">
          <table>
            <thead><tr><th>SKU</th><th>Producto</th><th>Disponible</th><th>Comprometido</th><th>Estado</th></tr></thead>
            <tbody>
              {productos.map(p => (
                <tr key={p.sku}>
                  <td className="mono td-main">{p.sku}</td>
                  <td className="td-main">{p.marca} — {p.presentacion}</td>
                  <td><span className="mono" style={{ color: p.stock > 0 ? C.color_exito : C.color_peligro }}>{p.stock}</span></td>
                  <td><span className="mono" style={{ color: C.color_acento }}>{p.comprometido}</span></td>
                  <td>{p.stock > 0 ? <Badge type="green">Disponible</Badge> : <Badge type="red">Sin stock</Badge>}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function CrearPlantilla() {
  const [marca, setMarca] = useState("");
  const [presentacion, setPresentacion] = useState("Six-pack");
  const [contenido, setContenido] = useState("");
  const [peso, setPeso] = useState("");
  const [errorMsg, setErrorMsg] = useState("");
  const [saved, setSaved] = useState(false);

  const completo = marca.trim() !== "" && contenido !== "" && peso !== "";

  const handleCrear = () => {
    if (!completo) return;
    if (parseFloat(peso) <= 0) {
      setErrorMsg("El peso logístico debe ser mayor a cero.");
      return;
    }
    const duplicado = productos.some(p => p.marca.toLowerCase() === marca.trim().toLowerCase() && p.presentacion === presentacion);
    if (duplicado) {
      const p = productos.find(p => p.marca.toLowerCase() === marca.trim().toLowerCase() && p.presentacion === presentacion);
      setErrorMsg(`Ya existe el producto ${p.marca} ${p.presentacion} (SKU: ${p.sku}). Por favor edítelo.`);
      return;
    }
    setErrorMsg("");
    setSaved(true);
  };

  const handleCancelar = () => {
    setMarca(""); setPresentacion("Six-pack"); setContenido(""); setPeso(""); setSaved(false); setErrorMsg("");
  };

  return (
    <div>
      <div className="page-header"><h1>Crear Plantilla de Producto</h1><p>FEAT-001 · Supervisor de Inventario</p></div>
      <div style={{ maxWidth: 480 }}>
        <div className="card">
          <div className="card-title"><span>＋</span> Nueva Plantilla</div>
          <div className="form-row">
            <label className="form-label">Marca <span className="required">*</span></label>
            <input className="form-input" placeholder="Ej: Águila, Heineken, Poker..." value={marca} onChange={e => { setMarca(e.target.value); setSaved(false); }} />
          </div>
          <div className="form-grid">
            <div className="form-row">
              <label className="form-label">Presentación <span className="required">*</span></label>
              <select className="form-select" value={presentacion} onChange={e => { setPresentacion(e.target.value); setSaved(false); }}>
                <option>Unidad</option><option>Six-pack</option><option>Caja</option><option>Barril</option><option>Estiba</option>
              </select>
            </div>
            <div className="form-row">
              <label className="form-label">Contenido (ml) <span className="required">*</span></label>
              <input className="form-input" type="number" placeholder="Ej: 330" value={contenido} onChange={e => { setContenido(e.target.value); setSaved(false); }} />
            </div>
          </div>
          <div className="form-row">
            <label className="form-label">Peso Logístico (kg) <span className="required">*</span></label>
            <input className="form-input" type="number" placeholder="Ej: 2.40" value={peso} onChange={e => { setPeso(e.target.value); setSaved(false); setErrorMsg(""); }} />
            <div className="hint">Peso bruto del empaque. Usado para cálculos de carga en flota.</div>
          </div>
          {errorMsg && <div className="alert alert-danger" style={{ marginBottom: 16 }}><span>✗</span> {errorMsg}</div>}
          <div className="btn-row">
            <button className="btn btn-primary" onClick={handleCrear} disabled={!completo} style={{ opacity: completo ? 1 : 0.5, cursor: completo ? "pointer" : "not-allowed" }}>
              Crear Plantilla
            </button>
            <button className="btn btn-secondary" onClick={handleCancelar}>Cancelar</button>
          </div>
        </div>

        {saved && (
          <div className="card" style={{ borderColor: "rgba(16,185,129,0.3)" }}>
            <div className="alert alert-success" style={{ marginBottom: 16 }}><span>✓</span> Plantilla creada exitosamente.</div>
            <div style={{ textAlign: "center", padding: "12px 0 20px" }}>
              <div style={{ fontSize: 11, color: C.color_texto_atenuado, marginBottom: 6, textTransform: "uppercase", letterSpacing: "1px" }}>SKU Generado</div>
              <div style={{ fontFamily: "'DM Mono', monospace", fontSize: 32, color: C.color_acento, fontWeight: 700 }}>SKU-006</div>
            </div>
            <div className="detail-row"><span className="detail-key">Marca</span><span className="detail-val">{marca}</span></div>
            <div className="detail-row"><span className="detail-key">Presentación</span><span className="detail-val">{presentacion}</span></div>
            <div className="detail-row"><span className="detail-key">Contenido</span><span className="detail-val mono">{contenido} ml</span></div>
            <div className="detail-row"><span className="detail-key">Peso logístico</span><span className="detail-val mono">{peso} kg</span></div>
            <div className="detail-row"><span className="detail-key">Stock inicial</span><span className="detail-val mono">0 unidades</span></div>
            <div className="detail-row"><span className="detail-key">Estado</span><span className="detail-val"><Badge type="blue">Listo para recepciones</Badge></span></div>
          </div>
        )}
      </div>
    </div>
  );
}

function ModificarPlantilla() {
  const [selected, setSelected] = useState(null);
  const [saved, setSaved] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");
  const [editMarca, setEditMarca] = useState("");
  const [editPresentacion, setEditPresentacion] = useState("");
  const [editContenido, setEditContenido] = useState("");
  const [editPeso, setEditPeso] = useState("");
  const [descripcionCambio, setDescripcionCambio] = useState("");
  const [bitacora, setBitacora] = useState([]);

  const handleSelect = (p) => {
    setSelected(p);
    setEditMarca(p.marca);
    setEditPresentacion(p.presentacion);
    setEditContenido(p.contenido);
    setEditPeso(p.peso);
    setSaved(false);
    setErrorMsg("");
    setDescripcionCambio("");
  };

  const hasLotes = selected && lotes.some(l => l.sku === selected.sku && (l.estado === "Disponible" || l.estado === "Comprometido"));

  const handleGuardar = () => {
    if (!descripcionCambio.trim()) {
      setErrorMsg("Debes agregar una descripción del cambio antes de guardar.");
      return;
    }
    if (parseFloat(editPeso) <= 0) {
      setErrorMsg("El peso logístico debe ser mayor a cero.");
      return;
    }
    const duplicado = productos.some(p => p.sku !== selected.sku && p.marca.toLowerCase() === editMarca.trim().toLowerCase() && p.presentacion === editPresentacion);
    if (duplicado) {
      setErrorMsg("La modificación generaría un duplicado de Marca + Presentación.");
      return;
    }
    const hoy = new Date().toLocaleDateString("es-CO");
    const nuevasEntradas = [];
    if (editMarca !== selected.marca) nuevasEntradas.push({ campo: "marca", anterior: selected.marca, nuevo: editMarca, descripcion: descripcionCambio, fecha: hoy });
    if (editPresentacion !== selected.presentacion) nuevasEntradas.push({ campo: "presentacion", anterior: selected.presentacion, nuevo: editPresentacion, descripcion: descripcionCambio, fecha: hoy });
    if (String(editContenido) !== String(selected.contenido)) nuevasEntradas.push({ campo: "contenido_ml", anterior: selected.contenido, nuevo: editContenido, descripcion: descripcionCambio, fecha: hoy });
    if (String(editPeso) !== String(selected.peso)) nuevasEntradas.push({ campo: "peso_logistico_kg", anterior: selected.peso, nuevo: editPeso, descripcion: descripcionCambio, fecha: hoy });
    setBitacora(prev => [...nuevasEntradas, ...prev]);
    setErrorMsg("");
    setSaved(true);
    setDescripcionCambio("");
  };

  return (
    <div>
      <div className="page-header"><h1>Modificar Plantilla de Producto</h1><p>FEAT-002 · Supervisor de Inventario</p></div>
      <div className="card">
        <div className="toolbar">
          <input className="search-bar" placeholder="Buscar por SKU, marca o presentación..." />
        </div>
        <div className="table-wrap">
          <table>
            <thead><tr><th>SKU</th><th>Marca</th><th>Presentación</th><th>Contenido</th><th>Peso (kg)</th><th>Acción</th></tr></thead>
            <tbody>
              {productos.map(p => (
                <tr key={p.sku}>
                  <td className="mono td-main">{p.sku}</td>
                  <td className="td-main">{p.marca}</td>
                  <td>{p.presentacion}</td>
                  <td className="mono">{p.contenido} ml</td>
                  <td className="mono">{p.peso} kg</td>
                  <td><button className="btn btn-secondary btn-sm" onClick={() => handleSelect(p)}>Editar</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      {selected && (
        <div className="grid-2">
          <div className="card">
            <div className="card-title"><span>✎</span> Editando: {selected.sku}</div>
            {saved && <div className="alert alert-success"><span>✓</span> Cambios guardados correctamente.</div>}
            <div className="form-row"><label className="form-label">SKU (inmutable)</label><input className="form-input" value={selected.sku} disabled style={{ opacity: 0.5, cursor: "not-allowed" }} /></div>
            <div className="form-row"><label className="form-label">Marca <span className="required">*</span></label><input className="form-input" value={editMarca} onChange={e => { setEditMarca(e.target.value); setSaved(false); setErrorMsg(""); }} /></div>
            <div className="form-grid">
              <div className="form-row"><label className="form-label">Presentación <span className="required">*</span></label><select className="form-select" value={editPresentacion} onChange={e => { setEditPresentacion(e.target.value); setSaved(false); setErrorMsg(""); }}><option>Unidad</option><option>Six-pack</option><option>Caja</option><option>Barril</option><option>Estiba</option></select></div>
              <div className="form-row"><label className="form-label">Contenido (ml) <span className="required">*</span></label><input className="form-input" value={editContenido} onChange={e => { setEditContenido(e.target.value); setSaved(false); setErrorMsg(""); }} /></div>
            </div>
            <div className="form-row"><label className="form-label">Peso Logístico (kg) <span className="required">*</span></label><input className="form-input" type="number" value={editPeso} onChange={e => { setEditPeso(e.target.value); setSaved(false); setErrorMsg(""); }} /></div>
            <div className="alert alert-warn" style={{ marginTop: 8 }}><span>⚠</span> Modificar el peso logístico afecta los cálculos de flota del Módulo 2.</div>
            <div className="form-row" style={{ marginTop: 12 }}>
              <label className="form-label">Descripción del cambio <span className="required">*</span></label>
              <textarea className="form-input" rows={2} placeholder="Ej: Actualización de peso por nuevo proveedor..." value={descripcionCambio} onChange={e => { setDescripcionCambio(e.target.value); setSaved(false); setErrorMsg(""); }} style={{ resize: "vertical" }} />
              <div className="hint">Obligatorio para la trazabilidad en bitácora (FR-009).</div>
            </div>
            {errorMsg && <div className="alert alert-danger" style={{ marginTop: 16 }}><span>✗</span> {errorMsg}</div>}
            <div className="btn-row">
              <button className="btn btn-primary" onClick={handleGuardar}>Guardar Cambios</button>
              <button className="btn btn-danger btn-sm" disabled={hasLotes} style={{ opacity: hasLotes ? 0.5 : 1, cursor: hasLotes ? "not-allowed" : "pointer" }} title={hasLotes ? "No se puede eliminar un producto con lotes activos." : ""}>Eliminar Producto</button>
            </div>
            {hasLotes && <div className="hint" style={{ marginTop: 8 }}>El producto no puede ser eliminado porque tiene lotes en inventario.</div>}
          </div>
          <div className="card">
            <div className="card-title"><span>≡</span> Bitácora de Cambios</div>
            {bitacora.length === 0
              ? <div style={{ fontSize: 13, color: C.color_texto_atenuado, padding: "12px 0" }}>Sin cambios registrados en esta sesión.</div>
              : <table>
                <thead><tr><th>Campo</th><th>Anterior</th><th>Nuevo</th><th>Descripción</th><th>Fecha</th></tr></thead>
                <tbody>
                  {bitacora.map((b, i) => (
                    <tr key={i}>
                      <td>{b.campo}</td>
                      <td className="mono" style={{ color: C.color_peligro }}>{String(b.anterior)}</td>
                      <td className="mono" style={{ color: C.color_exito }}>{String(b.nuevo)}</td>
                      <td style={{ fontSize: 11, color: C.color_texto_atenuado }}>{b.descripcion}</td>
                      <td>{b.fecha}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            }
          </div>
        </div>
      )}
    </div>
  );
}

function ConsultarInventario() {
  const [selected, setSelected] = useState(null);
  const hoy = new Date();

  // Real calculations
  const totalDisp = lotes.filter(l => l.estado === "Disponible").reduce((a, l) => a + l.cantidad, 0);
  const totalComp = lotes.filter(l => l.estado === "Comprometido").reduce((a, l) => a + l.cantidad, 0);
  const lotesCriticos = lotes.filter(l => l.estado === "Disponible" && (new Date(l.vencimiento) - hoy) / (1000 * 60 * 60 * 24) <= 30).length; // Asumimos 30 días como crítico
  const lotesMalos = lotes.filter(l => l.estado === "Vencido" || l.estado === "Avería").length;

  return (
    <div>
      <div className="page-header"><h1>Consultar Inventario</h1><p>FEAT-005 · Supervisor de Inventario — Ordenado por FEFO</p></div>
      <div className="grid-4" style={{ marginBottom: 16 }}>
        <div className="stat-card"><div className="stat-label">Total unidades disp.</div><div className="stat-value green">{totalDisp}</div></div>
        <div className="stat-card"><div className="stat-label">Comprometidas</div><div className="stat-value amber">{totalComp}</div></div>
        <div className="stat-card"><div className="stat-label">Lotes críticos FEFO</div><div className="stat-value red">{lotesCriticos}</div></div>
        <div className="stat-card"><div className="stat-label">Lotes vencidos/avería</div><div className="stat-value red">{lotesMalos}</div></div>
      </div>
      <div className="card">
        <div className="toolbar">
          <input className="search-bar" placeholder="Buscar por SKU o marca..." />
          <select className="form-select" style={{ width: 160 }}><option>Todos los estados</option><option>Disponible</option><option>Comprometido</option><option>Vencido</option></select>

        </div>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Código Lote</th><th>SKU</th><th>Producto</th><th>Vencimiento</th><th>Cantidad</th><th>Estado</th><th>FEFO</th><th>Acción</th></tr></thead>
            <tbody>
              {lotes.slice().sort((a, b) => new Date(a.vencimiento) - new Date(b.vencimiento)).map(l => {
                const isCritical = l.estado === "Disponible" && (new Date(l.vencimiento) - hoy) / (1000 * 60 * 60 * 24) <= 30;
                return (
                  <tr key={l.codigo} className={isCritical ? "lote-row-critical" : ""}>
                    <td className="mono td-main">{l.codigo}</td>
                    <td className="mono">{l.sku}</td>
                    <td>{l.marca}</td>
                    <td className="mono" style={{ color: isCritical ? C.color_alerta : C.color_texto_secundario }}>{l.vencimiento}</td>
                    <td className="mono">{l.cantidad}</td>
                    <td>{estadoBadge(l.estado)}</td>
                    <td>{isCritical ? <Badge type="amber">⚠ Crítico</Badge> : <Badge type="gray">Normal</Badge>}</td>
                    <td><button className="btn btn-secondary btn-sm" onClick={() => setSelected(l)}>Ver</button></td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
      {selected && (
        <div className="modal-overlay" onClick={() => setSelected(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-title">Detalle Lote: {selected.codigo}</div>
            {[["SKU", selected.sku], ["Marca", selected.marca], ["Vencimiento", selected.vencimiento], ["Cantidad", selected.cantidad], ["Estado", selected.estado], ["Costo COP", `$${selected.costo.toLocaleString()}`]].map(([k, v]) => (
              <div key={k} className="detail-row"><span className="detail-key">{k}</span><span className="detail-val mono">{v}</span></div>
            ))}
            <div className="btn-row"><button className="btn btn-secondary" onClick={() => setSelected(null)}>Cerrar</button></div>
          </div>
        </div>
      )}
    </div>
  );
}

function Excepciones() {
  return (
    <div>
      <div className="page-header"><h1>Excepciones de Inventario</h1><p>FEAT-004 · Registro histórico de averías, vencimientos y discrepancias</p></div>
      <div className="grid-3" style={{ marginBottom: 16 }}>
        <div className="stat-card"><div className="stat-label">Averías</div><div className="stat-value red">1</div></div>
        <div className="stat-card"><div className="stat-label">Vencimientos</div><div className="stat-value amber">1</div></div>
        <div className="stat-card"><div className="stat-label">Diferencias</div><div className="stat-value amber">1</div></div>
      </div>
      <div className="card">
        <div className="card-title"><span>⚠</span> Excepciones Registradas</div>
        <div className="table-wrap">
          <table>
            <thead><tr><th>ID</th><th>Tipo</th><th>Lote</th><th>Cantidad</th><th>Origen</th><th>Usuario</th><th>Fecha</th><th>Estado</th></tr></thead>
            <tbody>
              {excepciones.map(e => (
                <tr key={e.id}>
                  <td className="mono td-main">{e.id}</td>
                  <td><Badge type={e.tipo === "Avería" ? "red" : e.tipo === "Vencimiento" ? "amber" : "blue"}>{e.tipo}</Badge></td>
                  <td className="mono">{e.lote}</td>
                  <td className="mono">{e.cantidad}</td>
                  <td>{e.origen}</td>
                  <td>{e.usuario}</td>
                  <td className="mono">{e.fecha}</td>
                  <td>{estadoBadge(e.estado)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function RegistrarIngreso() {
  const [step, setStep] = useState(1);
  const cantidadEsperada = 120;
  const hoy = new Date();

  const [cantidad, setCantidad] = useState("");
  const [fechaVenc, setFechaVenc] = useState("");
  const [modalVisible, setModalVisible] = useState(false);
  const [excepcionDesc, setExcepcionDesc] = useState("");
  const [excepcionTipo, setExcepcionTipo] = useState("");
  const [excepcionGuardada, setExcepcionGuardada] = useState(false);

  const [sku, setSku] = useState("SKU-001");
  const [loteCodigo, setLoteCodigo] = useState("");

  const detectarProblemas = () => {
    const problemas = [];
    if (cantidad !== "" && parseFloat(cantidad) <= 0) {
      problemas.push({ tipo: "Error", msg: "La cantidad debe ser mayor a cero.", block: true });
    } else if (cantidad !== "" && parseInt(cantidad) !== cantidadEsperada) {
      problemas.push({ tipo: "Diferencia de Inventario", msg: `Cantidad recibida (${cantidad}) difiere de la esperada (${cantidadEsperada} uds).` });
    }
    if (loteCodigo !== "" && lotes.some(l => l.codigo.toLowerCase() === loteCodigo.trim().toLowerCase() && l.sku === sku)) {
      problemas.push({ tipo: "Error", msg: `El lote ${loteCodigo} ya existe para ${sku}.`, block: true });
    }
    if (fechaVenc !== "") {
      const fv = new Date(fechaVenc);
      const diffMeses = (fv - hoy) / (1000 * 60 * 60 * 24 * 30);
      if (fv <= hoy) problemas.push({ tipo: "Error", msg: "La fecha de vencimiento debe ser posterior a la fecha actual.", block: true });
      else if (diffMeses < 3) problemas.push({ tipo: "Vencimiento", msg: `Fecha de vencimiento muy próxima (${Math.round(diffMeses)} meses). Requiere excepción.` });
    }
    return problemas;
  };

  const problemas = detectarProblemas();

  const isBlocked = problemas.some(p => p.block);

  const handleRegistrarLote = () => {
    if (loteCodigo.trim() === "" || cantidad === "" || fechaVenc === "") return;
    if (isBlocked) return;

    if (problemas.length > 0) {
      setExcepcionTipo(problemas[0].tipo);
      setExcepcionDesc("");
      setExcepcionGuardada(false);
      setModalVisible(true);
    } else {
      setStep(3);
    }
  };

  const handleConfirmarExcepcion = () => {
    setExcepcionGuardada(true);
    setTimeout(() => { setModalVisible(false); setStep(3); }, 800);
  };

  return (
    <div>
      <div className="page-header"><h1>Registrar Ingreso de Productos</h1><p>FEAT-003 · Operario de Recepción</p></div>
      <div className="steps" style={{ marginBottom: 24 }}>
        {[["1", "Seleccionar Manifiesto"], ["2", "Registrar Lote"], ["3", "Confirmar Recepción"]].map(([n, label], i) => (
          <div key={n} className="step">
            {i > 0 && <div className={`step-line ${step > i ? "done" : ""}`} />}
            <div className={`step-circle ${step > parseInt(n) ? "done" : step === parseInt(n) ? "active" : "pending"}`}>{step > parseInt(n) ? "✓" : n}</div>
            <span className={`step-label ${step === parseInt(n) ? "active" : ""}`}>{label}</span>
          </div>
        ))}
      </div>

      {step === 1 && (
        <div className="card">
          <div className="card-title"><span>◫</span> Paso 1: Seleccionar Manifiesto Activo</div>
          <div className="form-row"><label className="form-label">Número de Manifiesto <span className="required">*</span></label>
            <select className="form-select"><option>MAN-2026-003 — Planta Bogotá — 10/03/2026</option><option>MAN-2026-002 — Planta Medellín — 09/03/2026</option></select>
          </div>
          <div className="card" style={{ background: C.color_fondo, border: `1px solid ${C.color_borde}` }}>
            <div style={{ fontSize: 12, color: C.color_texto_atenuado, marginBottom: 8 }}>Contenido del Manifiesto</div>
            {[["SKU-001", "Águila Six-pack", 120], ["SKU-004", "Heineken Six-pack", 48]].map(([sku, prod, cant]) => (
              <div key={sku} className="detail-row"><span className="detail-key">{sku} — {prod}</span><span className="detail-val mono">{cant} uds</span></div>
            ))}
          </div>
          <div className="btn-row"><button className="btn btn-primary" onClick={() => setStep(2)}>Continuar →</button></div>
        </div>
      )}

      {step === 2 && (
        <div>
          <div className="card" style={{ maxWidth: 520 }}>
            <div className="card-title"><span>↓</span> Paso 2: Datos del Lote</div>
            <div className="form-row"><label className="form-label">SKU <span className="required">*</span></label>
              <select className="form-select" value={sku} onChange={e => setSku(e.target.value)}>
                <option value="SKU-001">SKU-001 — Águila Six-pack</option>
                <option value="SKU-004">SKU-004 — Heineken Six-pack</option>
              </select>
            </div>
            <div className="form-row"><label className="form-label">Código de Lote <span className="required">*</span></label>
              <input className="form-input" placeholder="Ej: LOT-2026-010" value={loteCodigo} onChange={e => setLoteCodigo(e.target.value)} />
            </div>
            <div className="form-grid">
              <div className="form-row">
                <label className="form-label">Cantidad Recibida <span className="required">*</span></label>
                <input className="form-input" type="number" placeholder={cantidadEsperada} value={cantidad}
                  onChange={e => setCantidad(e.target.value)} />
                <div className="hint">Esperada según manifiesto: {cantidadEsperada} uds</div>
              </div>
              <div className="form-row">
                <label className="form-label">Fecha Vencimiento <span className="required">*</span></label>
                <input className="form-input" type="date" value={fechaVenc} onChange={e => setFechaVenc(e.target.value)} />
              </div>
            </div>
            <div className="form-grid">
              <div className="form-row"><label className="form-label">Fecha Expedición <span className="required">*</span></label><input className="form-input" type="date" /></div>
              <div className="form-row"><label className="form-label">Costo COP <span className="required">*</span></label><input className="form-input" type="number" placeholder="2800" /></div>
            </div>
            {problemas.length > 0 && (
              <div style={{ marginTop: 4 }}>
                {problemas.map((p, i) => (
                  <div key={i} className={`alert alert-${p.block ? 'danger' : 'warn'}`}>
                    <span>{p.block ? '✗' : '⚠'}</span> {p.msg} {!p.block && "Se solicitará reporte de excepción antes de continuar."}
                  </div>
                ))}
              </div>
            )}
            <div className="btn-row">
              <button className="btn btn-primary" onClick={handleRegistrarLote} disabled={isBlocked || loteCodigo.trim() === "" || cantidad === "" || fechaVenc === ""} style={{ opacity: (isBlocked || loteCodigo.trim() === "" || cantidad === "" || fechaVenc === "") ? 0.5 : 1, cursor: (isBlocked || loteCodigo.trim() === "" || cantidad === "" || fechaVenc === "") ? "not-allowed" : "pointer" }}>Registrar Lote →</button>
              <button className="btn btn-secondary" onClick={() => setStep(1)}>← Atrás</button>
            </div>
          </div>

          {modalVisible && (
            <div className="modal-overlay">
              <div className="modal">
                <div className="modal-title">⚠ Reporte de Excepción Requerido</div>
                <div className="alert alert-warn" style={{ marginBottom: 16 }}>
                  <span>⚠</span>
                  <div>
                    <strong>{excepcionTipo}</strong><br />
                    {problemas.find(p => p.tipo === excepcionTipo)?.msg}
                  </div>
                </div>
                <div style={{ fontSize: 13, color: C.color_texto_secundario, marginBottom: 16 }}>
                  Para registrar este lote debes documentar la excepción. El reporte quedará registrado automáticamente junto con el lote.
                </div>
                <div className="form-row">
                  <label className="form-label">Tipo de Excepción</label>
                  <select className="form-select" value={excepcionTipo} onChange={e => setExcepcionTipo(e.target.value)}>
                    <option>Diferencia de Inventario</option>
                    <option>Vencimiento</option>
                    <option>Avería</option>
                  </select>
                </div>
                <div className="form-row">
                  <label className="form-label">Descripción <span className="required">*</span></label>
                  <textarea className="form-input" rows={3} placeholder="Describe la situación observada..."
                    value={excepcionDesc} onChange={e => setExcepcionDesc(e.target.value)}
                    style={{ resize: "vertical" }} />
                </div>
                {excepcionGuardada && <div className="alert alert-success"><span>✓</span> Excepción registrada. Continuando...</div>}
                <div className="btn-row">
                  <button className="btn btn-danger"
                    onClick={handleConfirmarExcepcion}
                    disabled={excepcionDesc.trim() === "" || excepcionGuardada}
                    style={{ opacity: excepcionDesc.trim() === "" ? 0.5 : 1 }}>
                    Registrar Excepción y Continuar
                  </button>
                  <button className="btn btn-secondary" onClick={() => setModalVisible(false)}>Cancelar</button>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {step === 3 && (
        <div className="card" style={{ maxWidth: 520 }}>
          <div className="card-title"><span>✓</span> Paso 3: Confirmación</div>
          <div className="alert alert-success"><span>✓</span><div>Recepción registrada exitosamente. <strong>REC-2026-003</strong> generado.</div></div>
          {problemas.length > 0 && <div className="alert alert-warn"><span>⚠</span> Excepción <strong>{excepcionTipo}</strong> registrada junto con el lote.</div>}
          {[["SKU", "SKU-001"], ["Lote", "LOT-2026-010"], ["Cantidad", `${cantidad || cantidadEsperada} uds`], ["Estado", "Disponible"], ["Movimiento", "MovInv tipo Entrada registrado"]].map(([k, v]) => (
            <div key={k} className="detail-row"><span className="detail-key">{k}</span><span className="detail-val">{v}</span></div>
          ))}
          <div className="btn-row"><button className="btn btn-primary" onClick={() => { setStep(1); setCantidad(""); setFechaVenc(""); }}>Nueva Recepción</button></div>
        </div>
      )}
    </div>
  );
}

function ReportarExcepcion() {
  const [tipo, setTipo] = useState("Avería");
  const [origen, setOrigen] = useState("Inspección");
  const [saved, setSaved] = useState(false);
  return (
    <div>
      <div className="page-header"><h1>Reportar Excepción de Inventario</h1><p>FEAT-004 · Operario de Recepción / Supervisor / Operario de Picking</p></div>
      <div className="grid-2">
        <div className="card">
          <div className="card-title"><span>!</span> Nueva Excepción</div>
          {saved && <div className="alert alert-success"><span>✓</span> Excepción registrada. Stock actualizado y Supervisor notificado.</div>}
          <div className="form-row"><label className="form-label">Tipo de Excepción <span className="required">*</span></label>
            <select className="form-select" value={tipo} onChange={e => setTipo(e.target.value)}>
              <option>Avería</option><option>Vencimiento</option><option>Diferencia de Inventario</option>
            </select>
          </div>
          <div className="form-row"><label className="form-label">Origen <span className="required">*</span></label>
            <select className="form-select" value={origen} onChange={e => setOrigen(e.target.value)}>
              <option>Inspección</option><option>Recepción</option><option>Picking</option>
            </select>
          </div>
          <div className="form-row"><label className="form-label">Lote Afectado <span className="required">*</span></label>
            <select className="form-select">
              {lotes.filter(l => l.estado === "Disponible" || (l.estado === "Comprometido" && origen === "Picking")).map(l => <option key={l.codigo}>{l.codigo} — {l.marca} [{l.estado}] ({l.cantidad} uds)</option>)}
            </select>
            {origen === "Picking" && <div className="hint" style={{ color: C.color_alerta }}>⚠ En modo Picking también se muestran lotes comprometidos (US-3 spec_reportar_excepciones).</div>}
          </div>
          <div className="form-row"><label className="form-label">Cantidad Afectada <span className="required">*</span></label><input className="form-input" type="number" placeholder="Ej: 12" /><div className="hint">No puede superar el stock actual del lote.</div></div>
          <div className="form-row"><label className="form-label">Descripción <span className="required">*</span></label><textarea className="form-input" rows={3} placeholder="Descripción del daño o situación detectada..." style={{ resize: "vertical" }} /></div>
          <div className="btn-row"><button className="btn btn-danger" onClick={() => setSaved(true)}>Registrar Excepción</button><button className="btn btn-secondary">Cancelar</button></div>
        </div>
        <div>
          <div className="card">
            <div className="card-title"><span>ℹ</span> Impacto según Tipo</div>
            <div className="alert alert-info" style={{ marginBottom: 10 }}>
              {tipo === "Avería" && "El stock del lote se descuenta inmediatamente. Si es total, el lote queda bloqueado en estado Avería."}
              {tipo === "Vencimiento" && "El lote completo cambia a estado Vencido y queda bloqueado para cualquier asignación futura."}
              {tipo === "Diferencia de Inventario" && "El lote se crea con la cantidad física real. La diferencia queda registrada para gestión del Supervisor."}
            </div>
          </div>
          <div className="card">
            <div className="card-title"><span>≡</span> Últimas Excepciones</div>
            {excepciones.slice(0, 3).map(e => (
              <div key={e.id} className="detail-row">
                <span className="detail-key"><Badge type={e.tipo === "Avería" ? "red" : "amber"}>{e.tipo}</Badge> {e.lote}</span>
                <span className="detail-val mono">{e.fecha}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

function ListarPedidos() {
  const [selected, setSelected] = useState(null);
  const [view, setView] = useState("Comprometidos");
  const [filtroCliente, setFiltroCliente] = useState("");
  const [filtroFechaDesde, setFiltroFechaDesde] = useState("");
  const [filtroFechaHasta, setFiltroFechaHasta] = useState("");

  const comprometidos = pedidos.filter(p => p.estado === "Comprometido").sort((a, b) => new Date(a.fecha) - new Date(b.fecha));
  const esperando = pedidos.filter(p => p.estado === "Esperando Ruta").sort((a, b) => new Date(a.fecha) - new Date(b.fecha));

  const aplicarFiltros = (lista) => lista.filter(p => {
    const coincideCliente = !filtroCliente || p.cliente.toLowerCase().includes(filtroCliente.toLowerCase());
    const coincideDesde = !filtroFechaDesde || p.fecha >= filtroFechaDesde;
    const coincideHasta = !filtroFechaHasta || p.fecha <= filtroFechaHasta;
    return coincideCliente && coincideDesde && coincideHasta;
  });

  const listado = aplicarFiltros(view === "Comprometidos" ? comprometidos : esperando);
  return (
    <div>
      <div className="page-header"><h1>Pedidos {view}</h1><p>FEAT-009 · Operario de Picking / Módulo 2 simulación — Ordenados por fecha ASC (FIFO)</p></div>
      <div className="grid-3" style={{ marginBottom: 16 }}>
        <div className="stat-card" style={{ cursor: "pointer", border: view === "Comprometidos" ? `2px solid ${C.color_acento}` : "none" }} onClick={() => setView("Comprometidos")}><div className="stat-label">Comprometidos (Listos)</div><div className="stat-value amber">{comprometidos.length}</div></div>
        <div className="stat-card" style={{ cursor: "pointer", border: view === "Esperando Ruta" ? `2px solid ${C.color_acento}` : "none" }} onClick={() => setView("Esperando Ruta")}><div className="stat-label">Esperando Ruta Módulo 2</div><div className="stat-value">{esperando.length}</div></div>
        <div className="stat-card"><div className="stat-label">Valor total visible</div><div className="stat-value" style={{ fontSize: 18 }}>${listado.reduce((a, p) => a + p.total, 0).toLocaleString()}</div></div>
      </div>
      <div className="toolbar" style={{ flexWrap: "wrap", gap: 8 }}>
        <input className="search-bar" placeholder="Buscar por cliente..." value={filtroCliente} onChange={e => setFiltroCliente(e.target.value)} />
        <div style={{ display: "flex", alignItems: "center", gap: 6, fontSize: 12, color: C.color_texto_atenuado }}>
          <span>Desde:</span><input type="date" className="form-input" style={{ width: 150, padding: "6px 10px" }} value={filtroFechaDesde} onChange={e => setFiltroFechaDesde(e.target.value)} />
          <span>Hasta:</span><input type="date" className="form-input" style={{ width: 150, padding: "6px 10px" }} value={filtroFechaHasta} onChange={e => setFiltroFechaHasta(e.target.value)} />
        </div>
        {(filtroCliente || filtroFechaDesde || filtroFechaHasta) && <button className="btn btn-secondary btn-sm" onClick={() => { setFiltroCliente(""); setFiltroFechaDesde(""); setFiltroFechaHasta(""); }}>✕ Limpiar</button>}
      </div>
      {listado.length === 0
        ? <div className="empty-state"><div className="icon">✓</div><p>No hay pedidos en la vista seleccionada.</p></div>
        : listado.map(p => (
          <div key={p.numero} className="card" style={{ cursor: "pointer", borderColor: selected?.numero === p.numero ? C.color_acento : C.color_borde }} onClick={() => setSelected(selected?.numero === p.numero ? null : p)}>
            <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: selected?.numero === p.numero ? 16 : 0 }}>
              <div style={{ flex: 1 }}>
                <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                  <span className="td-main" style={{ fontSize: 14, fontWeight: 700 }}>{p.numero}</span>
                  {estadoBadge(p.estado)}
                </div>
                <div style={{ fontSize: 12, color: C.color_texto_atenuado, marginTop: 4 }}>{p.cliente} · NIT: {p.nit} · {p.fecha}</div>
              </div>
              <div style={{ textAlign: "right" }}>
                <div style={{ fontFamily: "'DM Mono'", color: C.color_exito, fontWeight: 700 }}>${p.total.toLocaleString()}</div>
                <div style={{ fontSize: 11, color: C.color_texto_atenuado }}>{p.productos.length} línea(s)</div>
              </div>
            </div>
            {selected?.numero === p.numero && (
              <div>
                <hr className="divider" />
                <div style={{ fontSize: 12, color: C.color_texto_atenuado, marginBottom: 10 }}>Dirección entrega: Calle 72 #10-34, Bogotá</div>
                <table>
                  <thead><tr><th>SKU</th><th>Producto</th><th>Cantidad</th><th>Lote Asignado</th></tr></thead>
                  <tbody>
                    {p.productos.map((pr, i) => <tr key={i}><td className="mono">{pr.sku}</td><td>{pr.nombre}</td><td className="mono">{pr.cantidad}</td><td className="mono">{pr.lote}</td></tr>)}
                  </tbody>
                </table>
                <div className="btn-row">
                  {view === "Comprometidos" ? (
                    <button className="btn btn-primary btn-sm">Iniciar Picking →</button>
                  ) : (
                    <button className="btn btn-primary btn-sm">Asignar Ruta (Módulo 2) → Comprometer</button>
                  )}
                </div>
              </div>
            )}
          </div>
        ))
      }
    </div>
  );
}

function ConfirmarPicking() {
  const [pedidoSel, setPedidoSel] = useState(null);
  const [confirmado, setConfirmado] = useState(false);
  const [parcial, setParcial] = useState(false);
  const candidatos = pedidos.filter(p => p.estado === "Comprometido");
  return (
    <div>
      <div className="page-header"><h1>Confirmar Picking</h1><p>FEAT-010 · Operario de Picking</p></div>
      <div className="grid-2">
        <div className="card">
          <div className="card-title"><span>≡</span> Seleccionar Pedido</div>
          {candidatos.map(p => (
            <div key={p.numero} onClick={() => { setPedidoSel(p); setConfirmado(false); setParcial(false); }}
              style={{ padding: "12px", borderRadius: 8, border: `1px solid ${pedidoSel?.numero === p.numero ? C.color_acento : C.color_borde}`, cursor: "pointer", marginBottom: 8, background: pedidoSel?.numero === p.numero ? C.color_acento_brillo : "transparent" }}>
              <div style={{ display: "flex", justifyContent: "space-between" }}>
                <span style={{ fontWeight: 600 }}>{p.numero}</span>{estadoBadge(p.estado)}
              </div>
              <div style={{ fontSize: 12, color: C.color_texto_atenuado, marginTop: 4 }}>{p.cliente} · {p.productos.length} línea(s)</div>
            </div>
          ))}
        </div>
        {pedidoSel && (
          <div className="card">
            <div className="card-title"><span>✓</span> Confirmar Picking: {pedidoSel.numero}</div>
            {confirmado && <div className="alert alert-success"><span>✓</span> Picking confirmado. Lotes → En Picking. MovInventario registrado.</div>}
            {parcial && <div className="alert alert-warn"><span>⚠</span> Faltante detectado. Sistema buscará lote sustituto. Supervisor notificado.</div>}
            {parcial && (
              <div className="alert alert-warn" style={{ marginBottom: 16 }}>
                <span>⚠</span>
                <div>
                  <strong>Faltante detectado</strong>
                  <div style={{ fontSize: 13, marginTop: 4 }}>
                    El sistema ha buscado stock en otros lotes disponibles y ha reasignado 12 unidades del lote <strong>LOT-2026-004</strong> para completar el pedido.
                    <br />Se generó un MovimientoInventario tipo "Faltante_Picking" y el Supervisor fue notificado.
                  </div>
                </div>
              </div>
            )}
            <table style={{ marginBottom: 16 }}>
              <thead><tr><th>SKU</th><th>Producto</th><th>Lote Asignado</th><th>Esperado</th><th>Real</th></tr></thead>
              <tbody>
                {pedidoSel.productos.map((pr, i) => (
                  <tr key={i}>
                    <td className="mono">{pr.sku}</td><td>{pr.nombre}</td><td className="mono">{pr.lote}</td>
                    <td className="mono">{pr.cantidad}</td>
                    <td><input className="form-input" type="number" defaultValue={parcial && i === 0 ? pr.cantidad - 12 : pr.cantidad} style={{ width: 80, padding: "4px 8px" }} /></td>
                  </tr>
                ))}
                {parcial && (
                  <tr>
                    <td className="mono">{pedidoSel.productos[0].sku}</td><td>{pedidoSel.productos[0].nombre}</td><td className="mono" style={{ color: C.color_exito }}>LOT-2026-004</td>
                    <td className="mono" style={{ color: C.color_texto_atenuado }}>-</td>
                    <td><input className="form-input" type="number" defaultValue={12} style={{ width: 80, padding: "4px 8px" }} /></td>
                  </tr>
                )}
              </tbody>
            </table>
            <div className="btn-row">
              <button className="btn btn-primary" onClick={() => { setConfirmado(true); setParcial(false); }}>Confirmar Picking Completo</button>
              <button className="btn btn-secondary" onClick={() => setParcial(true)}>Simular Reportar Faltante</button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

function ConfirmarDespacho() {
  const [pedidoSel, setPedidoSel] = useState(null);
  const [confirmado, setConfirmado] = useState(false);
  const [modoParcial, setModoParcial] = useState(false);
  const [cantidadesReales, setCantidadesReales] = useState({});
  const candidatos = pedidos.filter(p => p.estado === "En Picking");

  const handleSeleccionar = (p) => {
    setPedidoSel(p);
    setConfirmado(false);
    setModoParcial(false);
    const init = {};
    p.productos.forEach(pr => { init[pr.sku] = pr.cantidad; });
    setCantidadesReales(init);
  };

  const esParcial = pedidoSel && pedidoSel.productos.some(pr => cantidadesReales[pr.sku] < pr.cantidad);

  return (
    <div>
      <div className="page-header"><h1>Confirmar Despacho</h1><p>FEAT-011 · Operario de Despacho</p></div>
      <div className="grid-2">
        <div className="card">
          <div className="card-title"><span>⬆</span> Pedidos En Picking</div>
          {candidatos.length === 0
            ? <div className="empty-state"><div className="icon">◉</div><p>No hay pedidos listos para despacho.</p></div>
            : candidatos.map(p => (
              <div key={p.numero} onClick={() => handleSeleccionar(p)}
                style={{ padding: "12px", borderRadius: 8, border: `1px solid ${pedidoSel?.numero === p.numero ? C.color_acento : C.color_borde}`, cursor: "pointer", marginBottom: 8, background: pedidoSel?.numero === p.numero ? C.color_acento_brillo : "transparent" }}>
                <div style={{ display: "flex", justifyContent: "space-between" }}>
                  <span style={{ fontWeight: 600 }}>{p.numero}</span>{estadoBadge(p.estado)}
                </div>
                <div style={{ fontSize: 12, color: C.color_texto_atenuado, marginTop: 4 }}>{p.cliente}</div>
              </div>
            ))
          }
        </div>
        {pedidoSel && (
          <div className="card">
            <div className="card-title"><span>⬆</span> Despachar: {pedidoSel.numero}</div>
            {confirmado ? (
              <>
                <div className="alert alert-success"><span>✓</span> Pedido despachado. Estado → Despachado. MovimientoInventario "Salida" registrado.</div>
                {esParcial && <div className="alert alert-warn" style={{ marginTop: 8 }}><span>⚠</span> Despacho <strong>Parcial</strong> registrado. Se registraron solo las cantidades reales despachadas.</div>}
                <div className="detail-row"><span className="detail-key">Fecha/hora despacho</span><span className="detail-val mono">{new Date().toLocaleString("es-CO")}</span></div>
              </>
            ) : (
              <>
                <table style={{ marginBottom: 12 }}>
                  <thead><tr><th>SKU</th><th>Producto</th><th>Solicitado</th><th>Real a despachar</th></tr></thead>
                  <tbody>
                    {pedidoSel.productos.map((pr, i) => (
                      <tr key={i}>
                        <td className="mono">{pr.sku}</td>
                        <td>{pr.nombre}</td>
                        <td className="mono">{pr.cantidad}</td>
                        <td>
                          {modoParcial
                            ? <input type="number" className="form-input" style={{ width: 80, padding: "4px 8px" }}
                              value={cantidadesReales[pr.sku] ?? pr.cantidad}
                              min={0} max={pr.cantidad}
                              onChange={e => setCantidadesReales(c => ({ ...c, [pr.sku]: parseInt(e.target.value) || 0 }))}
                            />
                            : <span className="mono">{pr.cantidad}</span>
                          }
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                <div className="detail-row"><span className="detail-key">Cliente</span><span className="detail-val">{pedidoSel.cliente}</span></div>
                <div className="detail-row"><span className="detail-key">NIT</span><span className="detail-val mono">{pedidoSel.nit}</span></div>
                {esParcial && <div className="alert alert-warn" style={{ marginTop: 8 }}><span>⚠</span> ¡Alerta! Las cantidades ingresadas son menores a las solicitadas. Se registrará como <strong>Despacho Parcial</strong>.</div>}
                <div className="btn-row">
                  <button className="btn btn-primary" onClick={() => setConfirmado(true)}>{esParcial ? "Confirmar Despacho Parcial ⬆" : "Confirmar Despacho Completo ⬆"}</button>
                  {!modoParcial && <button className="btn btn-secondary" onClick={() => setModoParcial(true)}>Editar cantidades (Parcial)</button>}
                </div>
              </>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

function Catalogo() {
  const [busqueda, setBusqueda] = useState("");
  const filtrados = productos.filter(p =>
    p.marca.toLowerCase().includes(busqueda.toLowerCase()) ||
    p.presentacion.toLowerCase().includes(busqueda.toLowerCase())
  );
  const precios = { "SKU-001": 28000, "SKU-002": 65000, "SKU-003": 180000, "SKU-004": 52000, "SKU-005": 95000 };
  return (
    <div>
      <div className="page-header">
        <h1>Catálogo de Productos</h1>
        <p>FEAT-007 · Asesor Comercial — Consulta el catálogo y realiza pedidos para tus clientes</p>
      </div>
      <div className="toolbar">
        <input className="search-bar" placeholder="🔍  Buscar por marca o presentación..." value={busqueda} onChange={e => setBusqueda(e.target.value)} />
      </div>
      <div className="grid-3">
        {filtrados.map(p => (
          <div key={p.sku} className="card" style={{
            borderColor: p.stock > 0 ? C.color_borde : "rgba(239,68,68,0.15)",
            opacity: p.stock === 0 ? 0.7 : 1,
            transition: "box-shadow 0.2s",
          }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: 14 }}>
              <div>
                <div style={{ fontWeight: 700, fontSize: 16, color: C.color_texto }}>{p.marca}</div>
                <div style={{ fontSize: 12, color: C.color_texto_atenuado, marginTop: 3 }}>{p.presentacion} · {p.contenido} ml</div>
              </div>
              {p.stock > 0
                ? <span style={{ width: 10, height: 10, borderRadius: "50%", background: C.color_exito, display: "inline-block", marginTop: 5, boxShadow: `0 0 6px ${C.color_exito}` }} />
                : <span style={{ width: 10, height: 10, borderRadius: "50%", background: C.color_peligro, display: "inline-block", marginTop: 5 }} />
              }
            </div>
            <div style={{ fontFamily: "'DM Mono'", fontSize: 24, fontWeight: 700, color: C.color_texto, marginBottom: 2 }}>
              ${(precios[p.sku] || 0).toLocaleString("es-CO")}
            </div>
            <div style={{ fontSize: 11, color: C.color_texto_atenuado, marginBottom: 16 }}>por unidad de empaque</div>
            <div style={{ marginBottom: 14 }}>
              {p.stock > 0
                ? <span style={{ fontSize: 12, color: C.color_exito, fontWeight: 500 }}>✓ Disponible para pedido</span>
                : <span style={{ fontSize: 12, color: C.color_peligro, fontWeight: 500 }}>✗ Temporalmente sin existencias</span>
              }
            </div>
            <button
              className={`btn ${p.stock > 0 ? "btn-primary" : "btn-secondary"} btn-sm`}
              style={{ width: "100%" }}
              disabled={p.stock === 0}
            >
              {p.stock > 0 ? "Agregar al pedido" : "No disponible"}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}

function Step2Entrega({ lineas, cantidades, precios, total, onConfirmar, onVolver }) {
  const clientes = [
    { cc: "800123456", nombre: "Supermercado El Rey", nit: "800.123.456-1", tel: "3001234567", direccion: "Av. El Dorado #68-10, Bogotá" },
    { cc: "900456789", nombre: "Tienda La Esquina", nit: "900.456.789-2", tel: "3009876543", direccion: "Cra 7 #45-23, Medellín" },
    { cc: "111222333", nombre: "Distribuidora Inactiva", nit: "111.222.333-0", tel: "3005551234", direccion: "Calle 80 #12-34, Cali" },
  ];
  const [doc, setDoc] = useState("");
  const [cliente, setCliente] = useState(null);
  const [buscando, setBuscando] = useState(false);
  const [noEncontrado, setNoEncontrado] = useState(false);
  const [errorConectividad, setErrorConectividad] = useState(false);
  const [verificandoDisp, setVerificandoDisp] = useState(false);
  const [stockInsuficiente, setStockInsuficiente] = useState([]);

  const buscar = () => {
    setBuscando(true);
    setNoEncontrado(false);
    setCliente(null);
    setErrorConectividad(false);
    setTimeout(() => {
      const found = clientes.find(c => c.cc === doc.replace(/\D/g, ""));
      setBuscando(false);
      if (found) setCliente(found);
      else setNoEncontrado(true);
    }, 600);
  };

  const simularErrorConectividad = () => {
    setBuscando(true);
    setCliente(null);
    setNoEncontrado(false);
    setTimeout(() => {
      setBuscando(false);
      setErrorConectividad(true);
    }, 800);
  };

  const isClienteValido = cliente && cliente.nombre !== "Distribuidora Inactiva";

  const handleConfirmar = () => {
    setVerificandoDisp(true);
    setStockInsuficiente([]);
    setTimeout(() => {
      // Simular verificación: Poker (SKU-003) siempre tiene stock=0
      const sinStock = lineas.filter(p => p.stock === 0);
      setVerificandoDisp(false);
      if (sinStock.length > 0) {
        setStockInsuficiente(sinStock);
      } else {
        onConfirmar(cliente);
      }
    }, 1000);
  };

  return (
    <div className="grid-2">
      <div className="card">
        <div className="card-title"><span>✓</span> Resumen del pedido</div>
        {lineas.map(p => (
          <div key={p.sku} className="detail-row">
            <span className="detail-key">{p.marca} — {p.presentacion}<br /><span style={{ fontSize: 11 }}>{cantidades[p.sku]} unidad(es)</span></span>
            <span className="detail-val mono">${((precios[p.sku] || 0) * cantidades[p.sku]).toLocaleString("es-CO")}</span>
          </div>
        ))}
        <div style={{ display: "flex", justifyContent: "space-between", marginTop: 16, paddingTop: 12, borderTop: `1px solid ${C.color_borde}` }}>
          <span style={{ fontWeight: 700, color: C.color_texto }}>Total del pedido</span>
          <span style={{ fontFamily: "'DM Mono'", fontWeight: 700, color: C.color_exito, fontSize: 18 }}>${total.toLocaleString("es-CO")}</span>
        </div>
        {stockInsuficiente.length > 0 && (
          <div className="alert alert-danger" style={{ marginTop: 12 }}>
            <span>✗</span>
            <div>
              <strong>Stock insuficiente</strong>
              {stockInsuficiente.map(p => <div key={p.sku}>Stock insuficiente para: <strong>{p.marca}</strong>. Disponible: 0 unidades (FR-055).</div>)}
            </div>
          </div>
        )}
        <div className="btn-row">
          <button className="btn btn-primary" onClick={handleConfirmar}
            disabled={!isClienteValido || verificandoDisp}
            style={{ opacity: isClienteValido && !verificandoDisp ? 1 : 0.5, cursor: isClienteValido && !verificandoDisp ? "pointer" : "not-allowed" }}>
            {verificandoDisp ? "⏳ Verificando disponibilidad..." : "Confirmar pedido ✓"}
          </button>
          <button className="btn btn-secondary" onClick={onVolver}>← Modificar</button>
        </div>
        {!cliente && <div style={{ fontSize: 11, color: C.color_texto_atenuado, marginTop: 8 }}>Ingresa el documento (CC/NIT) del cliente para continuar.</div>}
      </div>

      <div className="card">
        <div className="card-title"><span>📍</span> Datos del Cliente</div>
        <div className="form-row">
          <label className="form-label">Cédula / NIT <span className="required">*</span></label>
          <div style={{ display: "flex", gap: 8 }}>
            <input
              className="form-input"
              placeholder="Ej: 800123456"
              value={doc}
              onChange={e => { setDoc(e.target.value); setCliente(null); setNoEncontrado(false); }}
              onKeyDown={e => e.key === "Enter" && buscar()}
            />
            <button className="btn btn-primary btn-sm" onClick={buscar} style={{ whiteSpace: "nowrap" }}>
              {buscando ? "..." : "Buscar"}
            </button>
          </div>
          <div className="hint">Prueba: 800123456 · 900456789</div>
        </div>

        {noEncontrado && (
          <div className="alert alert-danger"><span>✗</span> No encontramos ese documento en el módulo de usuarios. Verifica e intenta de nuevo.</div>
        )}
        {errorConectividad && (
          <div className="alert alert-danger"><span>✗</span> <strong>Error de conectividad</strong> con el módulo de usuarios. No se puede continuar. Intenta nuevamente (FR-046).</div>
        )}
        {!errorConectividad && <button className="btn btn-secondary btn-sm" onClick={simularErrorConectividad} style={{ marginTop: 4, fontSize: 11 }}>🛠 Simular error conectividad</button>}

        {cliente && (
          <div style={{ marginTop: 4 }}>
            {cliente.nombre === "Distribuidora Inactiva" ? (
              <div className="alert alert-danger" style={{ marginBottom: 12 }}><span>✗</span> El cliente se encuentra en estado INACTIVO. No se puede proceder.</div>
            ) : (
              <div className="alert alert-success" style={{ marginBottom: 12 }}><span>✓</span> Datos cargados correctamente. Cliente Activo.</div>
            )}
            <div className="form-row">
              <label className="form-label">Nombre / Razón social</label>
              <input className="form-input" value={cliente.nombre} readOnly style={{ opacity: 0.75 }} />
            </div>
            <div className="form-row">
              <label className="form-label">Dirección de entrega</label>
              <input className="form-input" value={cliente.direccion} readOnly style={{ opacity: 0.75 }} />
            </div>
            <div className="form-row">
              <label className="form-label">Teléfono de contacto</label>
              <input className="form-input" value={cliente.tel} readOnly style={{ opacity: 0.75 }} />
            </div>
            {cliente.nombre !== "Distribuidora Inactiva" && <div style={{ fontSize: 11, color: C.color_texto_atenuado }}>Si algún dato del cliente es incorrecto, debe ser actualizado en el módulo de usuarios.</div>}
          </div>
        )}
      </div>
    </div>
  );
}

function RealizarPedido() {
  const [step, setStep] = useState(1);
  const [cantidades, setCantidades] = useState({});
  const [clienteConfirmado, setClienteConfirmado] = useState(null);
  const [pedidoNum] = useState("PED-2026-006");
  const precios = { "SKU-001": 28000, "SKU-002": 65000, "SKU-003": 180000, "SKU-004": 52000, "SKU-005": 95000 };
  const disponibles = productos.filter(p => p.stock > 0);
  const lineas = disponibles.filter(p => cantidades[p.sku] > 0);
  const total = lineas.reduce((acc, p) => acc + (precios[p.sku] || 0) * (cantidades[p.sku] || 0), 0);

  const handleConfirmado = (clienteData) => {
    setClienteConfirmado(clienteData);
    setStep(3);
  };

  return (
    <div>
      <div className="page-header">
        <h1>Realizar Pedido</h1>
        <p>FEAT-008 · Asesor Comercial — Consulta el cliente por CC/NIT y selecciona los productos para el pedido</p>
      </div>
      <div className="steps" style={{ marginBottom: 24 }}>
        {[["1", "Seleccionar productos"], ["2", "Revisar pedido"], ["3", "Pedido enviado"]].map(([n, label], i) => (
          <div key={n} className="step">
            {i > 0 && <div className={`step-line ${step > i ? "done" : ""}`} />}
            <div className={`step-circle ${step > parseInt(n) ? "done" : step === parseInt(n) ? "active" : "pending"}`}>
              {step > parseInt(n) ? "✓" : n}
            </div>
            <span className={`step-label ${step === parseInt(n) ? "active" : ""}`}>{label}</span>
          </div>
        ))}
      </div>

      {step === 1 && (
        <div className="grid-2">
          <div className="card">
            <div className="card-title"><span>🛒</span> Productos disponibles</div>
            {disponibles.map(p => (
              <div key={p.sku} style={{ display: "flex", alignItems: "center", gap: 12, padding: "12px 0", borderBottom: `1px solid ${C.color_borde}` }}>
                <div style={{ flex: 1 }}>
                  <div style={{ fontWeight: 600, color: C.color_texto }}>{p.marca}</div>
                  <div style={{ fontSize: 12, color: C.color_texto_atenuado }}>{p.presentacion} · {p.contenido} ml</div>
                  <div style={{ fontSize: 12, color: C.color_exito, marginTop: 2 }}>${(precios[p.sku] || 0).toLocaleString("es-CO")} / unidad</div>
                </div>
                <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  <button className="btn btn-secondary btn-sm" style={{ padding: "4px 10px", fontSize: 16 }}
                    onClick={() => setCantidades(c => ({ ...c, [p.sku]: Math.max(0, (c[p.sku] || 0) - 1) }))}>−</button>
                  <span style={{ fontFamily: "'DM Mono'", width: 28, textAlign: "center", color: C.color_texto, fontWeight: 700 }}>
                    {cantidades[p.sku] || 0}
                  </span>
                  <button className="btn btn-primary btn-sm" style={{ padding: "4px 10px", fontSize: 16 }}
                    onClick={() => setCantidades(c => ({ ...c, [p.sku]: (c[p.sku] || 0) + 1 }))}>+</button>
                </div>
              </div>
            ))}
          </div>
          <div className="card">
            <div className="card-title"><span>≡</span> Tu pedido</div>
            {lineas.length === 0
              ? <div className="empty-state"><div className="icon">🛒</div><p>Agrega productos para continuar.</p></div>
              : <>
                {lineas.map(p => (
                  <div key={p.sku} className="detail-row">
                    <span className="detail-key">{p.marca} × {cantidades[p.sku]}</span>
                    <span className="detail-val mono">${((precios[p.sku] || 0) * cantidades[p.sku]).toLocaleString("es-CO")}</span>
                  </div>
                ))}
                <div style={{ display: "flex", justifyContent: "space-between", marginTop: 16, paddingTop: 12, borderTop: `1px solid ${C.color_borde}` }}>
                  <span style={{ fontWeight: 700, color: C.color_texto }}>Total</span>
                  <span style={{ fontFamily: "'DM Mono'", fontWeight: 700, color: C.color_exito, fontSize: 16 }}>${total.toLocaleString("es-CO")}</span>
                </div>
                <div className="btn-row">
                  <button className="btn btn-primary" style={{ width: "100%" }} onClick={() => setStep(2)}>Revisar pedido →</button>
                </div>
              </>
            }
          </div>
        </div>
      )}

      {step === 2 && <Step2Entrega lineas={lineas} cantidades={cantidades} precios={precios} total={total} onConfirmar={handleConfirmado} onVolver={() => setStep(1)} />}

      {step === 3 && clienteConfirmado && (
        <div style={{ maxWidth: 620, margin: "0 auto" }}>
          <div className="card" style={{ padding: "32px" }}>
            <div style={{ textAlign: "center", marginBottom: 24 }}>
              <div style={{ fontSize: 48, marginBottom: 12 }}>🎉</div>
              <div style={{ fontSize: 22, fontWeight: 700, color: C.color_texto, marginBottom: 6 }}>¡Pedido registrado!</div>
              <div style={{ fontSize: 13, color: C.color_texto_atenuado }}>El pedido ha sido creado exitosamente y queda en espera de asignación de ruta.</div>
            </div>

            <div style={{ background: C.color_fondo, borderRadius: 10, padding: "16px 20px", marginBottom: 16 }}>
              <div style={{ fontSize: 11, color: C.color_texto_atenuado, textTransform: "uppercase", letterSpacing: "1px", marginBottom: 10 }}>Datos del Pedido</div>
              <div className="detail-row"><span className="detail-key">Número de pedido</span><span className="detail-val mono" style={{ color: C.color_acento }}>{pedidoNum}</span></div>
              <div className="detail-row"><span className="detail-key">Fecha de registro</span><span className="detail-val mono">{new Date().toLocaleDateString("es-CO")}</span></div>
              <div className="detail-row"><span className="detail-key">Total</span><span className="detail-val mono" style={{ color: C.color_exito, fontWeight: 700 }}>${total.toLocaleString("es-CO")}</span></div>
              <div className="detail-row"><span className="detail-key">Estado</span><span className="detail-val"><Badge type="blue">Esperando Ruta</Badge></span></div>
              <div className="detail-row"><span className="detail-key">Fecha estimada entrega</span><span className="detail-val"><Badge type="gray">En espera — pendiente Módulo 2</Badge></span></div>
            </div>

            <div style={{ background: C.color_fondo, borderRadius: 10, padding: "16px 20px", marginBottom: 16 }}>
              <div style={{ fontSize: 11, color: C.color_texto_atenuado, textTransform: "uppercase", letterSpacing: "1px", marginBottom: 10 }}>Cliente</div>
              <div className="detail-row"><span className="detail-key">Nombre / Razón Social</span><span className="detail-val">{clienteConfirmado.nombre}</span></div>
              <div className="detail-row"><span className="detail-key">CC / NIT</span><span className="detail-val mono">{clienteConfirmado.nit}</span></div>
              <div className="detail-row"><span className="detail-key">Dirección de entrega</span><span className="detail-val">{clienteConfirmado.direccion}</span></div>
              <div className="detail-row"><span className="detail-key">Teléfono de contacto</span><span className="detail-val mono">{clienteConfirmado.tel}</span></div>
            </div>

            <div style={{ background: C.color_fondo, borderRadius: 10, padding: "16px 20px", marginBottom: 16 }}>
              <div style={{ fontSize: 11, color: C.color_texto_atenuado, textTransform: "uppercase", letterSpacing: "1px", marginBottom: 10 }}>Productos del Pedido</div>
              <table style={{ width: "100%" }}>
                <thead><tr><th>SKU</th><th>Producto</th><th>Cantidad</th><th>Subtotal</th></tr></thead>
                <tbody>
                  {lineas.map(p => (
                    <tr key={p.sku}>
                      <td className="mono">{p.sku}</td>
                      <td>{p.marca} — {p.presentacion}</td>
                      <td className="mono">{cantidades[p.sku]} uds</td>
                      <td className="mono" style={{ color: C.color_exito }}>${((precios[p.sku] || 0) * cantidades[p.sku]).toLocaleString("es-CO")}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="alert alert-info" style={{ marginBottom: 12 }}>
              <span>⏳</span> <strong>Solicitud de ruta enviada al Módulo 2</strong> — Se está procesando la asignación de ruta y fecha de despacho.
            </div>

            <button className="btn btn-secondary" style={{ width: "100%" }} onClick={() => { setStep(1); setCantidades({}); setClienteConfirmado(null); }}>
              Nuevo pedido
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

function ListarManifiestos() {
  const manifiestos = [
    { id: "MAN-2026-003", origen: "Planta Bogotá", fecha: "2026-03-10", estado: "Activo", items: [{ sku: "SKU-001", producto: "Águila Six-pack", cantidad: 120 }, { sku: "SKU-004", producto: "Heineken Six-pack", cantidad: 48 }] },
    { id: "MAN-2026-002", origen: "Planta Medellín", fecha: "2026-03-09", estado: "Activo", items: [{ sku: "SKU-002", producto: "Club Colombia Caja", cantidad: 60 }] },
    { id: "MAN-2026-001", origen: "Planta Bogotá", fecha: "2026-03-05", estado: "Cerrado", items: [{ sku: "SKU-005", producto: "Costeña Estiba", cantidad: 312 }] },
  ];
  const [sel, setSel] = useState(null);
  const [filtroEstado, setFiltroEstado] = useState("Activo");
  const filtrados = manifiestos.filter(m => filtroEstado === "Todos" || m.estado === filtroEstado);
  return (
    <div>
      <div className="page-header"><h1>Listar Manifiestos</h1><p>FEAT · Operario de Recepción — Consulta los manifiestos vigentes antes de registrar un ingreso</p></div>
      <div className="grid-3" style={{ marginBottom: 16 }}>
        <div className="stat-card"><div className="stat-label">Manifiestos activos</div><div className="stat-value green">{manifiestos.filter(m => m.estado === "Activo").length}</div></div>
        <div className="stat-card"><div className="stat-label">Manifiestos cerrados</div><div className="stat-value">{manifiestos.filter(m => m.estado === "Cerrado").length}</div></div>
        <div className="stat-card"><div className="stat-label">SKUs en tránsito</div><div className="stat-value amber">{manifiestos.filter(m => m.estado === "Activo").reduce((a, m) => a + m.items.length, 0)}</div></div>
      </div>
      <div className="card">
        <div className="toolbar">
          <select className="form-select" style={{ width: 160 }} value={filtroEstado} onChange={e => setFiltroEstado(e.target.value)}>
            <option>Activo</option><option>Cerrado</option><option>Todos</option>
          </select>
        </div>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Manifiesto</th><th>Origen</th><th>Fecha</th><th>SKUs</th><th>Estado</th><th>Acción</th></tr></thead>
            <tbody>
              {filtrados.map(m => (
                <tr key={m.id}>
                  <td className="mono td-main">{m.id}</td>
                  <td>{m.origen}</td>
                  <td className="mono">{m.fecha}</td>
                  <td>{m.items.length} línea(s)</td>
                  <td>{m.estado === "Activo" ? <Badge type="green">Activo</Badge> : <Badge type="gray">Cerrado</Badge>}</td>
                  <td><button className="btn btn-secondary btn-sm" onClick={() => setSel(sel?.id === m.id ? null : m)}>Ver detalle</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      {sel && (
        <div className="modal-overlay" onClick={() => setSel(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <div className="modal-title">📋 Manifiesto: {sel.id}</div>
            <div className="detail-row"><span className="detail-key">Origen</span><span className="detail-val">{sel.origen}</span></div>
            <div className="detail-row"><span className="detail-key">Fecha</span><span className="detail-val mono">{sel.fecha}</span></div>
            <div className="detail-row"><span className="detail-key">Estado</span><span className="detail-val">{sel.estado === "Activo" ? <Badge type="green">Activo</Badge> : <Badge type="gray">Cerrado</Badge>}</span></div>
            <hr className="divider" />
            <div style={{ fontSize: 12, color: C.color_texto_atenuado, marginBottom: 8 }}>Contenido del Manifiesto</div>
            <table>
              <thead><tr><th>SKU</th><th>Producto</th><th>Cantidad esperada</th></tr></thead>
              <tbody>
                {sel.items.map((item, i) => <tr key={i}><td className="mono">{item.sku}</td><td>{item.producto}</td><td className="mono">{item.cantidad} uds</td></tr>)}
              </tbody>
            </table>
            <div className="btn-row">
              {sel.estado === "Activo" && <button className="btn btn-primary" onClick={() => setSel(null)}>✓ Usar para Registrar Ingreso</button>}
              <button className="btn btn-secondary" onClick={() => setSel(null)}>Cerrar</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function ConsultarDetallePedido() {
  const [numero, setNumero] = useState("");
  const [result, setResult] = useState(null);
  const [actor, setActor] = useState("Módulo 2");
  const buscar = () => {
    const p = pedidos.find(p => p.numero === numero.trim());
    setResult(p || "notfound");
  };
  return (
    <div>
      <div className="page-header"><h1>Consultar Detalle de Pedido</h1><p>FEAT-012 · Integración Módulo 2 y Módulo 3</p></div>
      <div className="card">
        <div className="form-grid-3">
          <div className="form-row"><label className="form-label">Módulo Consultante</label>
            <select className="form-select" value={actor} onChange={e => setActor(e.target.value)}><option>Módulo 2</option><option>Módulo 3</option></select>
          </div>
          <div className="form-row"><label className="form-label">Número de Pedido</label>
            <input className="form-input" placeholder="Ej: PED-2024-005" value={numero} onChange={e => setNumero(e.target.value)} />
          </div>
          <div className="form-row"><label className="form-label">&nbsp;</label>
            <button className="btn btn-primary" onClick={buscar} style={{ width: "100%" }}>Consultar</button>
          </div>
        </div>
      </div>
      {result === "notfound" && <div className="alert alert-danger"><span>✗</span> Pedido no encontrado.</div>}
      {result && result !== "notfound" && (
        <div className="grid-2">
          <div className="card">
            <div className="card-title"><span>⊡</span> Datos Expuestos — {actor}</div>
            {(actor === "Módulo 2"
              ? [["Pedido N°", result.numero], ["Cliente", result.cliente], ["Dirección", "Cra 15 #80-20, Bogotá"], ["Estado", result.estado], ["Peso total (kg)", (result.productos.reduce((a, p) => a + productos.find(x => x.sku === p.sku)?.peso * p.cantidad, 0)).toFixed(2)]]
              : [["Pedido N°", result.numero], ["NIT Cliente", result.nit], ["Estado", result.estado], ["Tipo", "Completo"]]
            ).map(([k, v]) => <div key={k} className="detail-row"><span className="detail-key">{k}</span><span className="detail-val">{v}</span></div>)}
          </div>
          <div className="card">
            <div className="card-title"><span>≡</span> Líneas del Pedido</div>
            <table>
              <thead>
                <tr>
                  <th>SKU</th>
                  {actor !== "Módulo 2" && <th>Producto</th>}
                  <th>Solicitado</th>
                  {(actor === "Módulo 3" || actor === "Módulo 2") && <th>Despachado Real</th>}
                </tr>
              </thead>
              <tbody>
                {result.productos.map((p, i) => (
                  <tr key={i}>
                    <td className="mono">{p.sku}</td>
                    {actor !== "Módulo 2" && <td>{p.nombre}</td>}
                    <td className="mono">{p.cantidad}</td>
                    {(actor === "Módulo 3" || actor === "Módulo 2") && <td className="mono" style={{ color: C.color_exito }}>{p.cantidad - (i === 0 ? 1 : 0)}</td>}
                  </tr>
                ))}
              </tbody>
            </table>
            {actor === "Módulo 3" && (
              <div className="alert alert-warn" style={{ marginTop: 12 }}>
                <span>⚠</span> Pedido Parcial: La cantidad despachada difiere de la solicitada en una línea.
              </div>
            )}
            <div className="alert alert-info" style={{ marginTop: 12 }}><span>ℹ</span> Detalles internos de lotes (código, vencimiento) no expuestos a {actor}.</div>
          </div>
        </div>
      )}
    </div>
  );
}

function ConsultarCliente() {
  const [cc, setCc] = useState("");
  const [result, setResult] = useState(null);
  const clientes = [
    { cc: "800123456", nombre: "Supermercado El Rey", nit: "800.123.456-1", tel: "3001234567", direccion: "Av. El Dorado #68-10, Bogotá", estado: "Activo" },
    { cc: "900456789", nombre: "Tienda La Esquina", nit: "900.456.789-2", tel: "3009876543", direccion: "Cra 7 #45-23, Medellín", estado: "Activo" },
    { cc: "111222333", nombre: "Distribuidora Inactiva", nit: "111.222.333-0", tel: "3005551234", direccion: "Calle 80 #12-34, Cali", estado: "Inactivo" },
  ];
  const buscar = () => {
    const c = clientes.find(c => c.cc === cc.replace(/\D/g, ""));
    setResult(c || "notfound");
  };
  return (
    <div>
      <div className="page-header"><h1>Consultar Datos de Cliente por CC/NIT</h1><p>FEAT-006 · Asesor Comercial — Consulta al módulo externo de usuarios por CC/NIT para vincular al pedido</p></div>
      <div className="alert alert-info"><span>ℹ</span> Este módulo consulta al <strong>módulo externo de usuarios</strong> en tiempo real. Los datos del cliente no se almacenan localmente.</div>
      <div className="card">
        <div className="form-grid">
          <div className="form-row"><label className="form-label">Cédula / NIT del Cliente <span className="required">*</span></label>
            <input className="form-input" placeholder="Ej: 800123456" value={cc} onChange={e => setCc(e.target.value)} />
            <div className="hint">Prueba: 800123456 · 900456789 · 111222333</div>
          </div>
          <div className="form-row"><label className="form-label">&nbsp;</label>
            <button className="btn btn-primary" onClick={buscar} style={{ width: "100%" }}>Consultar Módulo Usuarios</button>
          </div>
        </div>
      </div>
      {result === "notfound" && <div className="alert alert-danger"><span>✗</span> No se encontró cliente con ese documento en el módulo de usuarios.</div>}
      {result && result !== "notfound" && (
        <div className="card">
          <div className="card-title"><span>👤</span> Datos del Cliente</div>
          {result.estado === "Inactivo" && <div className="alert alert-danger"><span>✗</span> Cliente INACTIVO. No puede ser vinculado a nuevos pedidos.</div>}
          {[["Nombre / Razón Social", result.nombre], ["NIT/CC", result.nit], ["Teléfono", result.tel], ["Dirección", result.direccion], ["Estado", result.estado]].map(([k, v]) => (
            <div key={k} className="detail-row">
              <span className="detail-key">{k}</span>
              <span className="detail-val">{k === "Estado" ? estadoBadge(result.estado) : v}</span>
            </div>
          ))}
          {result.estado === "Activo" && <div className="btn-row"><button className="btn btn-success">✓ Vincular a Pedido</button></div>}
        </div>
      )}
    </div>
  );
}

// ─── SCREEN MAP ───────────────────────────────────────────────────────────────
const screens = {
  "dashboard": <Dashboard />,
  "crear-plantilla": <CrearPlantilla />,
  "modificar-plantilla": <ModificarPlantilla />,
  "consultar-inventario": <ConsultarInventario />,
  "excepciones": <Excepciones />,
  "listar-manifiestos": <ListarManifiestos />,
  "registrar-ingreso": <RegistrarIngreso />,
  "reportar-excepcion": <ReportarExcepcion />,
  "listar-pedidos": <ListarPedidos />,
  "confirmar-picking": <ConfirmarPicking />,
  "confirmar-despacho": <ConfirmarDespacho />,
  "catalogo": <Catalogo />,
  "realizar-pedido": <RealizarPedido />,
  "consultar-detalle-pedido": <ConsultarDetallePedido />,
  "consultar-cliente": <ConsultarCliente />,
};

const actors = {
  "dashboard": "Supervisor",
  "crear-plantilla": "Supervisor",
  "modificar-plantilla": "Supervisor",
  "consultar-inventario": "Supervisor",
  "excepciones": "Supervisor",
  "listar-manifiestos": "Operario Recepción",
  "registrar-ingreso": "Operario Recepción",
  "reportar-excepcion": "Supervisor",
  "listar-pedidos": "Operario Picking",
  "confirmar-picking": "Operario Picking",
  "confirmar-despacho": "Operario Despacho",
  "catalogo": "Asesor Comercial",
  "realizar-pedido": "Asesor Comercial",
  "consultar-detalle-pedido": "Módulo 2 / 3",
  "consultar-cliente": "Asesor Comercial",
};

// ─── APP ─────────────────────────────────────────────────────────────────────
export default function App() {
  const [active, setActive] = useState("dashboard");

  return (
    <>
      <style>{styles}</style>
      <div className="layout">
        <aside className="sidebar">
          <div className="sidebar-logo">
            <div className="logo-badge">MÓDULO 1</div>
            <div className="logo-title">Distribución Mayorista</div>
            <div className="logo-sub">Gestión de Inventario</div>
          </div>
          {nav.map(section => (
            <div key={section.section} className="sidebar-section">
              <div className="sidebar-section-label">{section.section}</div>
              {section.items.map(item => (
                <div key={item.id} className={`nav-item ${active === item.id ? "active" : ""}`} onClick={() => setActive(item.id)}>
                  <span className="nav-icon">{item.icon}</span>
                  <span>{item.label}</span>
                  {item.badge && <span className={`nav-badge ${item.badgeClass || "nav-badge-warn"}`}>{item.badge}</span>}
                </div>
              ))}
            </div>
          ))}
        </aside>
        <div className="main">
          <div className="topbar">
            <span className="topbar-title">{nav.flatMap(s => s.items).find(i => i.id === active)?.label || "Dashboard"}</span>
            <span className="topbar-sub">— {actors[active]}</span>
            <div className="topbar-right">
              <span style={{ fontSize: 12, color: C.color_texto_atenuado }}>11/03/2026</span>
              <div className="avatar">SV</div>
            </div>
          </div>
          <div className="content">
            {screens[active]}
          </div>
        </div>
      </div>
    </>
  );
}
