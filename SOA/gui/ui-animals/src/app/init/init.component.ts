import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';

@Component({
  selector: 'app-init',
  standalone: true,
  templateUrl: './init.component.html',
})
export class InitComponent implements OnInit {

  constructor(private router: Router) {
  }

  ngOnInit(): void {
    const urlParams = new URLSearchParams(window.location.search);
    const token = urlParams.get('token');
    if (token) {
      localStorage.setItem('jwtToken', token);
      this.router.navigate(['data']);
    }
  }
}
