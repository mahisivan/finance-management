import axios from 'axios'
const api=axios.create({baseURL:import.meta.env.VITE_API_URL||'http://localhost:8000/api'})
api.interceptors.request.use(config=>{
 const token=localStorage.getItem('fna_token')
 if(token) config.headers.Authorization=`Bearer ${token}`
 return config
})
api.interceptors.response.use(r=>r,e=>{
 if(e.response?.status===401&&!e.config?.url?.includes('/auth/login')){
    localStorage.removeItem('fna_token');localStorage.removeItem('fna_user');window.location.href=`${import.meta.env.BASE_URL}login`
 }
 return Promise.reject(e)
})
export default api
