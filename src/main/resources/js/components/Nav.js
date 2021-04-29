import React, { Component } from 'react'; 
import { Link } from "react-router-dom";
import userService from "./services/User";
import "./Nav.css";


export default class Nav extends Component {

  constructor(){
    super();
    this.state = {
      isLogged: false,
      isAdmin: false,
      query:""
    }
  }

  async componentDidMount(){
    const res = await userService.getPrincipal()
    this.setState({
      isLogged: res.isLogged,
      isAdmin: res.isAdmin
    })
  }

  render() {
    let button1;
    let button2;
    if(this.state.isLogged != true){
      button1 = <a class="nav-link ml-auto" href="/login">Iniciar sesión</a>;
      button2 = <a class="nav-link" href="/register">Registrarme</a>;
    } else {
      button1 = <a  class="nav-link" href="/profile">Mi perfil</a>;
      button2 = <a  class="nav-link " href="/logout">Cerrar sesión</a>;
    }

    return (
      <nav style={{marginBottom:"30px"}}>
        <ul class="menu">
          <li ><a href="/">InfoBooks</a></li>
          {this.state.isLogged ? 
            <div><li ><a  href="#">Libros</a>
            <ul>
                <li ><a  href="/books/new">Añadir libro</a></li>
                <li ><a  href="/books/all/0">Cerca de mí</a></li>
                <li ><a  href="#">Filtrar por</a>
                  <ul>
                    <li ><a  href="/books/all/0/postalCode">Mi cod. postal</a></li>
                    <li ><a  href="/books/all/0/city">Mi ciudad</a></li>
                    <li ><a  href="/books/all/0/province">Mi provincia</a></li>
                  </ul>
                </li>
            </ul>
          </li>
          <li ><a  href="#">Peticiones</a>
            <ul>
                <li ><a  href="/requests/me/0">Enviadas</a></li>
                <li ><a  href="/requests/received/0">Recibidas</a></li>
            </ul>
          </li>
          <li style={{float:"right"}}><a href="#" >Perfil</a>
            <ul>
                <li ><a  href="/books/me/0">Mis libros</a></li>
                <li ><a  href="/favourites/0">Favoritos</a></li>
                <li >{button1}</li>
                <li >{button2}</li>
            </ul>
          </li>
          <li style={{float:"right"}}><div class="search-box">
            <input class="text" type="text" placeholder="Título, ISBN, año, ..."/>
          <button> <i class="fa fa-search"></i></button></div></li>
          </div>
        :
          <div><li style={{float:"left"}}><a  href="/books/all/0">Libros</a></li>
          <li style={{float:"right"}}>{button2}</li>
          <li style={{float:"right"}}>{button1}</li>
          <li style={{float:"right"}}><div class="search-box">
            <input class="text" type="text" placeholder="Título, ISBN, ..." onChange={(event) => this.setState({query:event.target.value})}/>
          <button type="button" onClick={() => this.search(this.state.query)} disabled={this.state.query.trim() == ''}> <i class="fa fa-search"></i></button></div></li>
          </div>
        }  
          
        </ul>
      </nav>
    )
  }

  async search(query) {
    window.location.replace("/search/"+query)
  }
}