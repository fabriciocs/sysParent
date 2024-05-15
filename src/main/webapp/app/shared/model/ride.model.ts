import dayjs from 'dayjs';
import { IChild } from 'app/shared/model/child.model';
import { IDriver } from 'app/shared/model/driver.model';
import { RideStatus } from 'app/shared/model/enumerations/ride-status.model';

export interface IRide {
  id?: number;
  scheduledTime?: dayjs.Dayjs;
  status?: keyof typeof RideStatus;
  pickupAddress?: string;
  dropoffAddress?: string;
  child?: IChild;
  driver?: IDriver;
}

export const defaultValue: Readonly<IRide> = {};
