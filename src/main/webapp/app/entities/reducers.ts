import parent from 'app/entities/parent/parent.reducer';
import driver from 'app/entities/driver/driver.reducer';
import child from 'app/entities/child/child.reducer';
import ride from 'app/entities/ride/ride.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  parent,
  driver,
  child,
  ride,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;
