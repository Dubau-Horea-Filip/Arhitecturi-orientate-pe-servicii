import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import { CommonModule } from '@angular/common';

export interface Animal {
  _id: { $oid: string };
  name: string;
  species: string;
  breed: string;
  age: number;
  health_status: string;
  adoption_status: boolean;
  arrival_date: { $date: number };
}

@Component({
  selector: 'app-data',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './data.component.html',
  styleUrls: ['./data.component.css'],
})
export class DataComponent implements OnInit, OnDestroy {
  animals: Animal[] = [];
  favouriteAnimals: string[] = [];
  private socket!: WebSocket; // WebSocket connection

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.refreshData();
    this.setupWebSocket(); // Initialize WebSocket connection
  }

  ngOnDestroy(): void {
    if (this.socket) {
      this.socket.close(); // Close WebSocket when component is destroyed
    }
  }

  refreshData() {
    this.gatherAnimals();
  }

  gatherAnimals() {
    const token = localStorage.getItem('jwtToken');
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }

    this.http.get<Animal[]>('http://localhost/animals/', { headers }).subscribe(
      (data) => {
        this.animals = data.map((item) => ({
          ...item,
          id: item._id.$oid,
        }));
      },
      (error) => {
        console.error("API Error:", error);
        if (error.status === 401) {
          this.hideContent();
        }
      }
    );
  }

  // gatherFavouriteAnimals() {
  //   const token = localStorage.getItem('jwtToken');
  //   let headers = new HttpHeaders();
  //   if (token) {
  //     headers = headers.set('Authorization', `Bearer ${token}`);
  //   }
  //
  //   this.http.get<string[]>('http://localhost/animals/favourites/', { headers }).subscribe(
  //     (data) => {
  //       this.favouriteAnimals = data;
  //     },
  //     (error) => {
  //       console.error("API Error:", error);
  //       if (error.status === 401) {
  //         this.hideContent();
  //       }
  //     }
  //   );
  // }

  adoptAnimal(animal: Animal) {
    const token = localStorage.getItem('jwtToken');
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }

    this.http.put(`http://localhost/animals/${animal._id.$oid}`, { adoption_status: true }, { headers })
      .subscribe(
        () => {
          animal.adoption_status = true;
        },
        (error) => {
          console.error('Error adopting animal:', error);
        }
      );
  }

  addToFavourites(animalId: string) {
    const token = localStorage.getItem('jwtToken');
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }

    this.http.post('http://localhost/animals/favourites/', { animal_id: animalId }, { headers })
      .subscribe(
        () => {
          this.favouriteAnimals.push(animalId);
        },
        (error) => {
          console.error('Error adding to favourites:', error);
        }
      );
  }

  removeFromFavourites(animalId: string) {
    const token = localStorage.getItem('jwtToken');
    let headers = new HttpHeaders();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }

    this.http.request('delete', 'http://localhost/animals/favourites/', { body: { animal_id: animalId }, headers })
      .subscribe(
        () => {
          this.favouriteAnimals = this.favouriteAnimals.filter((id) => id !== animalId);
        },
        (error) => {
          console.error('Error removing from favourites:', error);
        }
      );
  }

  hideContent() {
    document.body.innerHTML = 'You are not authorized to view this page';
  }

  /** 🔵 WebSocket Setup */
  private setupWebSocket() {
    this.socket = new WebSocket("ws://localhost:6789");

    this.socket.onopen = () => {
      console.log("Connected to WebSocket server");
    };

    this.socket.onmessage = (event) => {
      const data = JSON.parse(event.data);
      console.log("Received notification:", data);
      this.showNotification(data.message);
    };

    this.socket.onclose = () => {
      console.log("WebSocket connection closed, attempting to reconnect...");
      setTimeout(() => this.setupWebSocket(), 5000); // Reconnect after 5 seconds
    };

    this.socket.onerror = (error) => {
      console.error("WebSocket Error:", error);
    };
  }

  /** 🔔 Display a Notification (Browser Alert or Toast) */
  private showNotification(message: string) {
    alert(`Notification: ${message}`);
  }
}
