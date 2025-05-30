import dayjs from 'dayjs';
import { IUser } from 'app/shared/model/user.model';
import { IResource } from 'app/shared/model/resource.model';

export interface IReservation {
  id?: number;
  reservationDate?: dayjs.Dayjs;
  startTime?: dayjs.Dayjs;
  endTime?: dayjs.Dayjs;
  reservationId?: string;
  user?: IUser;
  resource?: IResource;
}

export const defaultValue: Readonly<IReservation> = {};
