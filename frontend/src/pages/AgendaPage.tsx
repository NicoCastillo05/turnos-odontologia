import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { useAuth } from '../features/auth/AuthContext';
import {
  listAppointments,
  createAppointment,
  updateAppointmentStatus,
  listProfessionals,
  listChairs,
  listServices,
} from '../features/agenda/api';
import type {
  AppointmentResponse,
  AppointmentStatus,
  ChairResponse,
  ProfessionalResponse,
  ServiceResponse,
} from '../features/agenda/types';

const STATUS_COLORS: Record<AppointmentStatus, string> = {
  scheduled: '#93c5fd',
  confirmed: '#86efac',
  cancelled: '#fca5a5',
  no_show: '#fcd34d',
  completed: '#c4b5fd',
};

function todayISO(): string {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

export default function AgendaPage(): JSX.Element {
  const { logout } = useAuth();
  const [date, setDate] = useState<string>(todayISO());
  const [professionalFilter, setProfessionalFilter] = useState<string>('');
  const [appointments, setAppointments] = useState<AppointmentResponse[]>([]);
  const [professionals, setProfessionals] = useState<ProfessionalResponse[]>([]);
  const [chairs, setChairs] = useState<ChairResponse[]>([]);
  const [services, setServices] = useState<ServiceResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // create form
  const [patientId, setPatientId] = useState<string>('');
  const [profId, setProfId] = useState<string>('');
  const [chairId, setChairId] = useState<string>('');
  const [serviceId, setServiceId] = useState<string>('');
  const [startAt, setStartAt] = useState<string>('');
  const [formError, setFormError] = useState<string | null>(null);

  useEffect(() => {
    void (async () => {
      try {
        const [pros, chs, svcs] = await Promise.all([
          listProfessionals(),
          listChairs(),
          listServices(),
        ]);
        setProfessionals(pros);
        setChairs(chs);
        setServices(svcs);
      } catch (e) {
        setError(e instanceof Error ? e.message : 'error cargando recursos');
      }
    })();
  }, []);

  useEffect(() => {
    void loadAppointments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [date, professionalFilter]);

  async function loadAppointments(): Promise<void> {
    setLoading(true);
    setError(null);
    try {
      const params: { from: string; to: string; professionalId?: number } = {
        from: `${date}T00:00:00`,
        to: `${date}T23:59:59`,
      };
      if (professionalFilter !== '') {
        params.professionalId = Number(professionalFilter);
      }
      const list = await listAppointments(params);
      list.sort((a, b) => a.startAt.localeCompare(b.startAt));
      setAppointments(list);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'error cargando turnos');
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(e: FormEvent<HTMLFormElement>): Promise<void> {
    e.preventDefault();
    setFormError(null);
    if (patientId === '' || profId === '' || chairId === '' || serviceId === '' || startAt === '') {
      setFormError('Todos los campos son obligatorios');
      return;
    }
    try {
      await createAppointment({
        patientId: Number(patientId),
        professionalId: Number(profId),
        chairId: Number(chairId),
        serviceId: Number(serviceId),
        startAt,
        allowOverbooking: false,
      });
      setPatientId('');
      setProfId('');
      setChairId('');
      setServiceId('');
      setStartAt('');
      await loadAppointments();
    } catch (err) {
      setFormError(err instanceof Error ? err.message : 'error creando turno');
    }
  }

  async function handleStatusChange(id: number, status: AppointmentStatus): Promise<void> {
    try {
      const updated = await updateAppointmentStatus(id, status);
      setAppointments((prev) => prev.map((a) => (a.id === id ? updated : a)));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'error actualizando estado');
    }
  }

  return (
    <main className="page">
      <header className="page-header">
        <h1>Agenda</h1>
        <button className="btn" onClick={logout}>Salir</button>
      </header>

      <section className="card">
        <div className="filters">
          <label className="field">
            Fecha
            <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
          </label>
          <label className="field">
            Profesional
            <select value={professionalFilter} onChange={(e) => setProfessionalFilter(e.target.value)}>
              <option value="">Todos</option>
              {professionals.map((p) => (
                <option key={p.id} value={p.id}>{p.specialty ?? `#${p.id}`}</option>
              ))}
            </select>
          </label>
        </div>
      </section>

      {error !== null && <p className="error">{error}</p>}
      {loading && <p>Cargando...</p>}

      <section className="card">
        <h2>Turnos</h2>
        {appointments.length === 0 && !loading ? (
          <p>Sin turnos</p>
        ) : (
          <ul className="appt-list">
            {appointments.map((a) => (
              <li key={a.id} className="appt-item" style={{ borderLeftColor: STATUS_COLORS[a.status] }}>
                <div className="appt-main">
                  <span className="appt-time">{a.startAt} – {a.endAt}</span>
                  <span className="appt-meta">Paciente #{a.patientId} · Prof #{a.professionalId} · Sillón #{a.chairId} · Servicio #{a.serviceId}</span>
                </div>
                <select
                  value={a.status}
                  onChange={(e) => void handleStatusChange(a.id, e.target.value as AppointmentStatus)}
                >
                  <option value="scheduled">scheduled</option>
                  <option value="confirmed">confirmed</option>
                  <option value="cancelled">cancelled</option>
                  <option value="no_show">no_show</option>
                  <option value="completed">completed</option>
                </select>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="card">
        <h2>Nuevo turno</h2>
        <form onSubmit={(e) => void handleCreate(e)} className="form grid">
          <label className="field">
            Paciente ID
            <input value={patientId} onChange={(e) => setPatientId(e.target.value)} />
          </label>
          <label className="field">
            Profesional
            <select value={profId} onChange={(e) => setProfId(e.target.value)}>
              <option value="">Seleccionar</option>
              {professionals.map((p) => (
                <option key={p.id} value={p.id}>{p.specialty ?? `#${p.id}`}</option>
              ))}
            </select>
          </label>
          <label className="field">
            Sillón
            <select value={chairId} onChange={(e) => setChairId(e.target.value)}>
              <option value="">Seleccionar</option>
              {chairs.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </label>
          <label className="field">
            Servicio
            <select value={serviceId} onChange={(e) => setServiceId(e.target.value)}>
              <option value="">Seleccionar</option>
              {services.map((s) => (
                <option key={s.id} value={s.id}>{s.name}</option>
              ))}
            </select>
          </label>
          <label className="field">
            Inicio (fecha y hora)
            <input type="datetime-local" value={startAt} onChange={(e) => setStartAt(e.target.value)} />
          </label>
          {formError !== null && <p className="error">{formError}</p>}
          <button type="submit" className="btn btn-primary">Crear turno</button>
        </form>
      </section>
    </main>
  );
}