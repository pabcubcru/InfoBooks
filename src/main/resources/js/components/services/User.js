const baseUrl = "http://localhost:8080"
import axios from "axios";
const user = {}

user.create = async(state) => {

    const datapost = {
        name: state.fieldName,
        email: state.fieldEmail,
        phone: state.fieldPhone,
        birthDate: state.fieldBirthDate,
        username: state.fieldUsername,
        password: state.fieldPassword,
    }

    const urlPost = baseUrl+"/register"
    const res = await axios.post(urlPost, datapost)
    .then(response => {return response.data;})
    .catch(error => {return error.response;})

    return res;
}

user.edit = async(state) => {

    const datapost = {
        name: state.fieldName,
        email: state.fieldEmail,
        phone: state.fieldPhone,
        birthDate: state.fieldBirthDate,
        username: state.fieldUsername,
        password: state.fieldPassword,
    }

    const urlPost = baseUrl+"/user/"+state.id+"/edit"
    const res = await axios.put(urlPost, datapost)
    .then(response => {return response.data;})
    .catch(error => {return error.response;})

    return res;
}

user.getUsername = async() => {
    const urlUsername = baseUrl+"/user/get-username"
    const res = await axios.get(urlUsername)
    .then(response => {return response.data})
    .catch(error => {return error})

    return res;
}

user.getPrincipal = async() => {
    const urlGetPrincipal = baseUrl+"/user/principal"
    const res = await axios.get(urlGetPrincipal)
    .then(response => {return response.data})
    .catch(error => {return error})

    return res;
}

user.getUser = async(username) => {
    const urlGet = baseUrl+"/user/"+username
    const res = await axios.get(urlGet)
    .then(response => {return response.data})
    .catch(error => {return error})

    return res;
}

export default user