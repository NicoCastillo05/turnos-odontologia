import { http } from '../../shared/api/http';
import type {
  AppointmentRequest,
  AppointmentResponse,
  AppointmentStatus,
  ChairResponse,
  ProfessionalResponse,
  ServiceResponse,
} from './types';

export async function listAppointments(params: {
  from?: string;
  to?: string;
  professionalId?: number;
  chairId?: number;
  status?: AppointmentStatus;
}): Promise<AppointmentResponse[]> {
  const qs = new URLSearchParams();
  if (params.from !== undefined) qs.set('from', params.from);
  if (params.to !== undefined) qs.set('to', params.to);
  if (params.professionalId !== undefined) qs.set('professionalId', String(params.professionalId));
  if (params.chairId !== undefined) qs.set('chairId', String(params.chairId));
  if (params.status !== undefined) qs.set('status', params.status);
  const suffix = qs.toString();
  return http<AppointmentResponse[]>(`/api/appointments${suffix ? `?${suffix}` : ''}`);
}

export async function createAppointment(req: AppointmentRequest): Promise<AppointmentResponse> {
  return http<AppointmentResponse>('/api/appointments', {
    method: 'POST',
    body: JSON.stringify(req),
  });
}

export async function updateAppointmentStatus(
  id: number,
  status: AppointmentStatus,
): Promise<AppointmentResponse> {
  return http<AppointmentResponse>(`/api/appointments/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  });
}

export async function listProfessionals(): Promise<ProfessionalResponse[]> {
  return http<ProfessionalResponse[]>('/api/professionals');
}

export async function listChairs(): Promise<ChairResponse[]> {
  return http<ChairResponse[]>('/api/chairs');
}

export async function listServices(): Promise<ServiceResponse[]> {
  return http<ServiceResponse[]>('/api/services');
}