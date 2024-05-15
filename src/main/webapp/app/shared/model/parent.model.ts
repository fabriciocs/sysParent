export interface IParent {
  id?: number;
  name?: string;
  phone?: string;
  email?: string;
  address?: string | null;
}

export const defaultValue: Readonly<IParent> = {};
