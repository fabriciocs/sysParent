import React from 'react';
import { Translate } from 'react-jhipster';

import MenuItem from 'app/shared/layout/menus/menu-item';

const EntitiesMenu = () => {
  return (
    <>
      {/* prettier-ignore */}
      <MenuItem icon="asterisk" to="/parent">
        <Translate contentKey="global.menu.entities.parent" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/driver">
        <Translate contentKey="global.menu.entities.driver" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/child">
        <Translate contentKey="global.menu.entities.child" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/ride">
        <Translate contentKey="global.menu.entities.ride" />
      </MenuItem>
      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;
