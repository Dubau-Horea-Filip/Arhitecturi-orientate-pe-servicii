import { Component } from '@angular/core';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import { CommonModule } from '@angular/common';

// Interface to model the Book class from backend
export interface Book {
  _id: { $oid: string };
  title: string;
  author: string;
  genre: string;
  price: number;
  quantity_available: number;
}

@Component({
  selector: 'app-book',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './databook.component.html',
  styleUrls: ['./databook.component.css'],
})
export class DataComponent {  // Ensure the class name matches the import
  books: Book[] = [];
  favouriteBooks: string[] = []; // Array to store favourite book IDs

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.refreshData();
  }

  refreshData() {
    this.gatherBooks();
    // Uncomment if gatherFavouriteBooks is defined
    // this.gatherFavouriteBooks();
  }

  // Fetch all books from backend
  // Fetch all books from backend
gatherBooks() {
  const token = localStorage.getItem('jwtToken');
  let headers = new HttpHeaders();
  if (token != null) {
    headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  this.http.get<any>('http://localhost/books/', { headers }).subscribe(
    (data) => {
      console.log("Raw API Response:", data);

      // Convert array of stringified JSON objects into real objects
      if (Array.isArray(data)) {
        this.books = data.map((item: string) => JSON.parse(item));
      } else {
        this.books = [];
      }

      console.log("Correctly Parsed Books:", this.books);
    },
    (error) => {
      console.error("API Error:", error);
      if (error.status === 401) {
        this.hideContent();
      }
    }
  );
}

  // Add book to favourites
  addToFavourites(bookId: string) {
    const token = localStorage.getItem('jwtToken');
    let headers = new HttpHeaders();
    if (token != null) {
      headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    }

    this.http
      .post('http://localhost/books/favourites/', { book_id: bookId }, { headers })
      .subscribe(
        () => {
          this.favouriteBooks.push(bookId); // Add the book ID to favourites
        },
        (error) => {
          console.error('Error adding to favourites:', error);
        }
      );
  }

  // Buy a book and decrease the available quantity
buyBook(bookId: string) {
  const token = localStorage.getItem('jwtToken');
  let headers = new HttpHeaders();
  if (token != null) {
    headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  this.http
    .post('http://localhost/books/buy/', { book_id: bookId }, { headers })
    .subscribe(
      (response: any) => {
        // Find the book and decrease the quantity available
        const book = this.books.find(b => b._id.$oid === bookId);
        if (book) {
          book.quantity_available -= 1;
        }
      },
      (error) => {
        console.error('Error buying the book:', error);
      }
    );
}

  // Remove book from favourites
  removeFromFavourites(bookId: string) {
    const token = localStorage.getItem('jwtToken');
    let headers = new HttpHeaders();
    if (token != null) {
      headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    }

    this.http
      .request('delete', 'http://localhost/books/favourites/', { body: { book_id: bookId }, headers })
      .subscribe(
        () => {
          this.favouriteBooks = this.favouriteBooks.filter((id) => id !== bookId); // Remove the book ID from favourites
        },
        (error) => {
          console.error('Error removing from favourites:', error);
        }
      );
  }

  // Hide the content if the user is not authorized
  hideContent() {
    document.body.innerHTML = 'You are not authorized to view this page';
  }
}
