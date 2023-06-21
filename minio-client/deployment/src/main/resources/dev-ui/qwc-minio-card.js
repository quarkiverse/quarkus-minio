import { LitElement, html, css} from 'lit';
import { pages } from 'build-time-data';
import { enabled } from 'build-time-data';
import 'qwc/qwc-extension-link.js';
import { JsonRpc } from 'jsonrpc'
import 'qui-badge';

export class QwcMinioCard extends LitElement {
    jsonRpc = new JsonRpc(this); 

    static styles = css`
      .identity {
        display: flex;
        justify-content: flex-start;
      }
      .loginDetails {
        padding-top: 10px;
        color: var(--lumo-contrast-70pct);
        font-size: smaller;
        text-align: center;
       }
      .description {
        padding-bottom: 10px;
      }
      .card-content {
        color: var(--lumo-contrast-90pct);
        display: flex;
        flex-direction: column;
        justify-content: flex-start;
        padding: 10px 10px;
        height: 100%;
      }
      .card-content slot {
        display: flex;
        flex-flow: column wrap;
        padding-top: 5px;
      }
    `;

    static properties = {
        extensionName: {type: String},
        description: {type: String},
        guide: {type: String},
        namespace: {type: String},
        _loginDetails: {type: String}
    }

    constructor() {
        super();
    }

    connectedCallback() {
        super.connectedCallback();
        this.jsonRpc.getLoginDetails().then(jsonRpcResponse => { 
            this._loginDetails = jsonRpcResponse.result;
        });
    }

    render() {
        return html`<div class="card-content" slot="content">
            <div class="identity">
                <div class="description">${this.description}</div>
            </div>
            ${this._renderCardLinks()}
            ${this._renderLoginDetails()}
        </div>
        `;
    }

    _renderCardLinks(){
        if(enabled){
        
            return html`${pages.map(page => html`
                            <qwc-extension-link slot="link"
                                namespace="${this.namespace}"
                                extensionName="${this.name}"
                                iconName="${page.icon}"
                                displayName="${page.title}"
                                staticLabel="${page.staticLabel}"
                                dynamicLabel="${page.dynamicLabel}"
                                streamingLabel="${page.streamingLabel}"
                                path="${page.id}"
                                ?embed=${page.embed}
                                externalUrl="${page.metadata.externalUrl}"
                                dynamicUrlMethodName="${page.metadata.dynamicUrlMethodName}"
                                webcomponent="${page.componentLink}" >
                            </qwc-extension-link>
                        `)}`;
        } else {
            return html`<span>Disabled</span>`;
        }
        
    }
    
    _renderLoginDetails(){
        if(this._loginDetails){
            return html`<div class="loginDetails">
                            <span>Access Key: <qui-badge><span>${this._loginDetails.accesskey}</span></qui-badge></span><br/>
                            <span>Secret Key: <qui-badge><span>${this._loginDetails.secretkey}</span></qui-badge></span>
                        </div>`;
        }
    }

}
customElements.define('qwc-minio-card', QwcMinioCard);