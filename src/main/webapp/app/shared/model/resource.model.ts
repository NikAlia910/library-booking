import { ResourceType } from 'app/shared/model/enumerations/resource-type.model';

export interface IResource {
  id?: number;
  title?: string;
  author?: string | null;
  keywords?: string | null;
  resourceType?: keyof typeof ResourceType;
}

export const defaultValue: Readonly<IResource> = {};
