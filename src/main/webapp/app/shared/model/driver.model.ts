export interface IDriver {
  id?: number;
  name?: string;
  phone?: string;
  email?: string;
  licenseNumber?: string;
}

export const defaultValue: Readonly<IDriver> = {};
