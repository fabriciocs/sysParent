import { IParent } from 'app/shared/model/parent.model';

export interface IChild {
  id?: number;
  name?: string;
  age?: number | null;
  schoolName?: string;
  parent?: IParent;
}

export const defaultValue: Readonly<IChild> = {};
