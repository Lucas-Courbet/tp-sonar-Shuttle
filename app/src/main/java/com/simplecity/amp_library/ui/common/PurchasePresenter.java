package com.simplecity.amp_library.ui.common;

public class PurchasePresenter<V extends PurchaseView> extends Presenter<V> {

    public void upgradeClicked() {
        PurchaseView purchaseView = getView();
        if (purchaseView != null) {
            purchaseView.showUpgradeDialog();
        }
    }
}