export type AppointmentStatus =
  | 'scheduled'
  | 'confirmed'
  | 'cancelled'
  | 'no_show'
  | 'completed';

export interface AppointmentResponse {
  id: number;
  tenantId: number;
  patientId: number;
  professionalId: number;
  chairId: number;
  serviceId: number;
  startAt: string;
  endAt: string;
  status: AppointmentStatus;
  overbooked: boolean;
  overbookAuthorizedByUserId: number | null;
  notes: string | null;
}

export interface AppointmentRequest {
  patientId: number;
  professionalId: number;
  chairId: number;
  serviceId: number;
  startAt: string;
  allowOverbooking: boolean;
  notes?: string;
}

export interface ProfessionalResponse {
  id: number;
  tenantId: number;
  userId: number | null;
  specialty: string | null;
  active: boolean;
}

export interface ChairResponse {
  id: number;
  tenantId: number;
  name: string;
  active: boolean;
}

export interface ServiceResponse {
  id: number;
  tenantId: number;
  name: string;
  durationMinutes: number;
  color: string | null;
  active: boolean;
}

export interface AppointmentStatusRequest {
  status: AppointmentStatus;
}