import { Component } from '@angular/core';
import { StravaConnectionComponent } from '../../../strava/components/strava-connection.component';
import { NikeConnectionComponent } from '../../../nike/components/nike-connection.component';

@Component({
  selector: 'app-user-profile',
  imports: [StravaConnectionComponent, NikeConnectionComponent],
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.scss'
})
export class UserProfileComponent {

}
