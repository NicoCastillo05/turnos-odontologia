export interface Professional {
  id: number;
  name: string;
}

export interface Service {
  id: number;
  name: string;
  durationMinutes: number;
}

export interface ClinicInfo {
  slug: string;
  name: string;
  professionals: Professional[];
  services: Service[];
}

export interface Slot {
  startAt: string;
  endAt: string;
}

export interface BookingRequest {
  professionalId: number;
  serviceId: number;
  startAt: string;
  patientName: string;
  patientPhone: string;
}

export interface BookingConfirmation {
  id: number;
  status: 'confirmed';
  professionalName: string;
  serviceName: string;
  startAt: string;
  endAt: string;
}

const API_BASE = import.meta.env.VITE_API_BASE ?? '';

export async function fetchClinic(slug: string): Promise<ClinicInfo> {
  const res = await fetch(`${API_BASE}/api/public/booking/${encodeURIComponent(slug)}`);
  if (!res.ok) {
    throw new Error('clínica no encontrada');
  }
  return res.json() as Promise<ClinicInfo>;
}

export async function fetchSlots(
  slug: string,
  professionalId: number,
  serviceId: number,
  date: string,
): Promise<Slot[]> {
  const params = new URLSearchParams({
    professionalId: String(professionalId),
    serviceId: String(serviceId),
    date,
  });
  const res = await fetch(
    `${API_BASE}/api/public/booking/${encodeURIComponent(slug)}/slots?${params.toString()}`,
  );
  if (!res.ok) {
    throw new Error('no se pudieron cargar los horarios');
  }
  return res.json() as Promise<Slot[]>;
}

export async function createBooking(slug: string, payload: BookingRequest): Promise<BookingConfirmation> {
  const res = await fetch(`${API_BASE}/api/public/booking/${encodeURIComponent(slug)}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || 'horario no disponible');
  }
  return res.json() as Promise<BookingConfirmation>;
}