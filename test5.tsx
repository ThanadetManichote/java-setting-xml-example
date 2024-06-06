// state.ts

import { atom, selector } from 'recoil';
import axios from 'axios';

export interface ApiResponse {
  id: number;
  name: string;
  // Add more properties as needed
}

export interface ApiDataState {
  data: ApiResponse[] | null;
  isLoading: boolean;
  error: string | null;
}

export const apiDataState = atom<ApiDataState>({
  key: 'apiDataState',
  default: {
    data: null,
    isLoading: false,
    error: null,
  },
});

export const fetchData = async (): Promise<ApiResponse[]> => {
  try {
    const response = await axios.get<ApiResponse[]>('https://api.example.com/data');
    return response.data;
  } catch (error) {
    throw new Error('Failed to fetch data');
  }
};

export const loadData = selector<ApiDataState>({
  key: 'loadData',
  get: async ({ get, set }) => {
    const apiData = get(apiDataState);
    if (!apiData.data && !apiData.isLoading) {
      try {
        set(apiDataState, { ...apiData, isLoading: true }); // Set isLoading to true
        const newData = await fetchData();
        // Set the fetched data and isLoading to false
        set(apiDataState, { data: newData, isLoading: false, error: null });
      } catch (error) {
        // Set error and isLoading to false if there's an error
        set(apiDataState, { ...apiData, error: error.message, isLoading: false });
      }
    }
    return get(apiDataState);
  },
});