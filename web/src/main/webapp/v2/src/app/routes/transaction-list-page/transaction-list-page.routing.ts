import { Routes } from '@angular/router';

import { UrlPath, UrlPathId } from 'app/shared/models';
import { TransactionListEmptyComponent } from './transaction-list-empty.component';
import { TransactionListBottomContentsContainerComponent } from 'app/core/components/transaction-list-bottom-contents/transaction-list-bottom-contents-container.component';
import { SystemConfigurationResolverService } from 'app/shared/services';
import { TransactionListPageComponent } from './transaction-list-page.component';

const TO_MAIN = '/' + UrlPath.MAIN;

export const routing: Routes = [
    {
        path: '',
        component: TransactionListPageComponent,
        resolve: {
            configuration: SystemConfigurationResolverService,
        },
        children: [
            {
                path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD + '/:' + UrlPathId.END_TIME + '/:' + UrlPathId.TRANSACTION_INFO + '/:' + UrlPathId.VIEW_TYPE,
                component: TransactionListBottomContentsContainerComponent,
            },
            {
                path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD + '/:' + UrlPathId.END_TIME + '/:' + UrlPathId.TRANSACTION_INFO,
                component: TransactionListBottomContentsContainerComponent,
            },
            {
                path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD + '/:' + UrlPathId.END_TIME,
                component: TransactionListEmptyComponent,
            },
            {
                path: ':' + UrlPathId.APPLICATION + '/:' + UrlPathId.PERIOD,
                redirectTo: TO_MAIN,
                pathMatch: 'full'
            },
            {
                path: ':' + UrlPathId.APPLICATION,
                redirectTo: TO_MAIN,
                pathMatch: 'full'
            },
            {
                path: '',
                redirectTo: TO_MAIN,
                pathMatch: 'full'
            }
        ]
    }
];
