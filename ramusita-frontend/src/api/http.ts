import axios from "axios";
import { API_BASE_URL } from "../config";


export const http = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // required for PLAYER_TOKEN cookie
});
