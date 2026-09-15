/*
 * Copyright (c) 2013-2024 the original author or authors.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package eapli.aisafe.app.common.console;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eapli.aisafe.Application;
import eapli.framework.infrastructure.pubsub.EventDispatcher;
import eapli.framework.infrastructure.pubsub.PubSubRegistry;
import eapli.framework.infrastructure.pubsub.impl.inprocess.service.InProcessPubSub;

/**
 * A base class for all console applications. This is an example of the Template
 * Method GoF design pattern.
 * <p>
 * <img src="template-method.svg">
 * </p>
 *
 * @author Paulo Gandra Sousa <!--
 *
 * @startuml template-method.svg
 *
 *           class ECafeteriaBaseApplication <<abstract>> {
 *
 *           +run()
 *
 *           +printFooter()
 *
 *           +printHeader()
 *
 *           + {abstract} doMain()
 *
 *           }
 *
 *           note right of ECafeteriaBaseApplication <font color="green">
 *
 *           //run() is the template method
 *
 *           void run() {
 *
 *           printHeader();
 *
 *           doMain();
 *
 *           printFooter();
 *
 *           }
 *
 *           end note
 *
 *           class ECafeteriaBackOfficeApplication {
 *
 *           +doMain()
 *
 *           +appTitle()
 *
 *           }
 *
 *           ECafeteriaBaseApplication <|-- ECafeteriaBackOfficeApplication
 *
 * @enduml -->
 */
@SuppressWarnings("squid:S106")
public abstract class AISafeBaseApplication {

	protected static final String SEPARATOR_HR = "============================================";
	private static final Logger LOGGER = LogManager.getLogger(AISafeBaseApplication.class);

	/**
	 * @param args the command line arguments
	 */
	public void run(final String[] args) {
		configure();

		clearTerminal();
		printHeader();

		try {
			setupEventHandlers();

			doMain(args);

			printFooter();
		} catch (final Exception e) {
			System.out.println(
					"Something unexpected has happened and the application will terminate. Please check the logs.\n");
			LOGGER.error(e);
		} finally {
			clearEventHandlers();
		}

		// exiting the application, closing all threads
		System.exit(0);
	}

	private static void clearTerminal() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}

	protected void printFooter() {
		System.out.println("\n");
		System.out.println(SEPARATOR_HR);
		System.out.println(appGoodbye());
		System.out.println(SEPARATOR_HR);
	}

	protected void printHeader() {
		System.out.println(SEPARATOR_HR);
		System.out.println(appTitle() + " " + Application.VERSION);
		System.out.println(Application.COPYRIGHT);
		System.out.println(SEPARATOR_HR);
	}

	private final void clearEventHandlers() {
		try {
			doClearEventHandlers(PubSubRegistry.dispatcher());

			PubSubRegistry.dispatcher().shutdown();
		} catch (final Exception e) {
			LOGGER.error("Unable to cleanup event handlers", e);
		}
	}

	private final void setupEventHandlers() {
		doSetupEventHandlers(PubSubRegistry.dispatcher());
	}

	protected void configure() {
		configureAuthz();

		configurePubSub();
	}

	protected void configurePubSub() {
		// TODO use a factory/registry to obtain the pub/sub engine
		/*
		 * SimplePersistentPubSub.configure(PersistenceContext.repositories().
		 * eventRecord(), PersistenceContext.repositories().eventConsumption(),
		 * Application.settings().getProperty("eapli.framework.pubsub.instanceKey"),
		 * Integer.valueOf(Application.settings().getProperty(
		 * "eapli.framework.pubsub.poolInterval") ));
		 * PubSubRegistry.configure(SimplePersistentPubSub.dispatcher(),
		 * SimplePersistentPubSub.publisher());
		 */
		PubSubRegistry.configure(InProcessPubSub.dispatcher(), InProcessPubSub.publisher());
	}

	protected abstract void configureAuthz();

	protected abstract void doMain(final String[] args);

	protected abstract String appTitle();

	protected abstract String appGoodbye();

	protected void doClearEventHandlers(final EventDispatcher dispatcher) {
		// nothing to do
	}

	protected abstract void doSetupEventHandlers(EventDispatcher dispatcher);
}
