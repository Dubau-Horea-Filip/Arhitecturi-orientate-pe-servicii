import {Routes} from '@angular/router';
import {InitComponent} from "./init/init.component";
import {DataComponent} from "./data/data.component";

export const routes: Routes = [
  {path: 'init', component: InitComponent},
  {path: 'data', component: DataComponent},
  {path: '**', redirectTo: 'init'}
];
