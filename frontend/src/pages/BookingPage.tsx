import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  fetchClinic,
  fetchSlots,
  createBooking,
  type ClinicInfo,
  type Slot,
  type BookingConfirmation,
} from '../features/booking/api';

export default function BookingPage(): JSX.Element {
  const { slug } = useParams<{ slug: string }>();
  const [clinic, setClinic] = useState<ClinicInfo | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [professionalId, setProfessionalId] = useState<number | null>(null);
  const [serviceId, setServiceId] = useState<number | null>(null);
  const [date, setDate] = useState<string>('');
  const [slots, setSlots] = useState<Slot[]>([]);
  const [selectedSlot, setSelectedSlot] = useState<Slot | null>(null);
  const [patientName, setPatientName] = useState<string>('');
  const [patientPhone, setPatientPhone] = useState<string>('');
  const [formError, setFormError] = useState<string | null>(null);
  const [confirmation, setConfirmation] = useState<BookingConfirmation | null>(null);

  useEffect(() => {
    if (slug === undefined) return;
    fetchClinic(slug)
      .then(setClinic)
      .catch(() => setError('clínica no encontrada'));
  }, [slug]);

  useEffect(() => {
    if (slug === undefined || professionalId === null || serviceId === null || date === '') return;
    fetchSlots(slug, professionalId, serviceId, date)
      .then(setSlots)
      .catch(() => setSlots([]));
  }, [slug, professionalId, serviceId, date]);

  if (error !== null) return <main className="page"><p className="error">{error}</p></main>;
  if (clinic === null) return <main className="page"><p>Cargando...</p></main>;

  async function handleConfirm(): Promise<void> {
    if (patientName.trim() === '' || patientPhone.trim() === '') {
      setFormError('Nombre y teléfono son obligatorios');
      return;
    }
    if (slug === undefined || professionalId === null || serviceId === null || selectedSlot === null) return;
    try {
      const conf = await createBooking(slug, {
        professionalId,
        serviceId,
        startAt: selectedSlot.startAt,
        patientName,
        patientPhone,
      });
      setConfirmation(conf);
    } catch (e) {
      setFormError(e instanceof Error ? e.message : 'horario no disponible');
    }
  }

  if (confirmation !== null) {
    return (
      <main className="page">
        <div className="card">
          <h1>Reserva confirmada</h1>
          <p>{confirmation.serviceName} con {confirmation.professionalName}</p>
          <p>{confirmation.startAt}</p>
        </div>
      </main>
    );
  }

  return (
    <main className="page">
      <div className="card">
        <h1>Reservar turno — {clinic.name}</h1>
        <div className="form">
          <label className="field">
            Profesional
            <select
              value={professionalId ?? ''}
              onChange={(e) => setProfessionalId(Number(e.target.value))}
            >
              <option value="">Seleccionar</option>
              {clinic.professionals.map((p) => (
                <option key={p.id} value={p.id}>{p.name}</option>
              ))}
            </select>
          </label>
          <label className="field">
            Prestación
            <select
              value={serviceId ?? ''}
              onChange={(e) => setServiceId(Number(e.target.value))}
            >
              <option value="">Seleccionar</option>
              {clinic.services.map((s) => (
                <option key={s.id} value={s.id}>{s.name}</option>
              ))}
            </select>
          </label>
          <label className="field">
            Fecha
            <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
          </label>
          <div className="form" style={{ flexDirection: 'row', flexWrap: 'wrap' }}>
            {slots.map((s) => (
              <button
                key={s.startAt}
                type="button"
                className="btn"
                onClick={() => setSelectedSlot(s)}
                style={{ fontWeight: selectedSlot?.startAt === s.startAt ? 'bold' : 'normal' }}
              >
                {s.startAt}
              </button>
            ))}
          </div>
          <label className="field">
            Nombre
            <input value={patientName} onChange={(e) => setPatientName(e.target.value)} />
          </label>
          <label className="field">
            Teléfono
            <input value={patientPhone} onChange={(e) => setPatientPhone(e.target.value)} />
          </label>
          {formError !== null && <p className="error">{formError}</p>}
          <button className="btn btn-primary" onClick={() => void handleConfirm()}>Confirmar reserva</button>
        </div>
      </div>
    </main>
  );
}