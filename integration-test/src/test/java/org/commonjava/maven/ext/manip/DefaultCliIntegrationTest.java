/*
 * Copyright (C) 2012 Red Hat, Inc. (jcasey@redhat.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.ext.manip;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;

import static org.commonjava.maven.ext.manip.TestUtils.DEFAULT_MVN_PARAMS;
import static org.commonjava.maven.ext.manip.TestUtils.IT_LOCATION;
import static org.commonjava.maven.ext.manip.TestUtils.getDefaultTestLocation;
import static org.commonjava.maven.ext.manip.TestUtils.runLikeInvoker;
import static org.commonjava.maven.ext.manip.TestUtils.runMaven;

@SuppressWarnings( "ConstantConditions" )
@RunWith( Parameterized.class )
public class DefaultCliIntegrationTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger( DefaultCliIntegrationTest.class );

    @Parameters( name = "{0}" )
    public static Collection<Object[]> getFiles()
    {
        Collection<Object[]> params = new ArrayList<>();
        // Hack to allow a single parameterized test to be run.
        if ( System.getProperties().containsKey( "test-cli" ) )
        {
            for (String t : System.getProperty("test-cli").split( "," ))
            {
                params.add( new Object[] { t } );
            }
        }
        else
        {
            for ( File rl : new File( IT_LOCATION ).listFiles() )
            {
                if ( rl.isDirectory() && !TestUtils.EXCLUDED_FILES.contains( rl.getName() ) )
                {
                    Object[] arr = new Object[] { rl.getName() };
                    params.add( arr );
                }
            }
        }

        return params;
    }

    private String testRelativeLocation;

    public DefaultCliIntegrationTest( String testRelativeLocation )
    {
        this.testRelativeLocation = testRelativeLocation;
    }

    @BeforeClass
    public static void setUp()
        throws Exception
    {
        for ( File setupTest : new File( getDefaultTestLocation( "setup" ) ).listFiles() )
        {
            LOGGER.info ("Running install for {}", setupTest.toString());

            // Try to do some simplistic checks to see if this has already been done.
            if ( ! setupExists( setupTest ))
            {
                runMaven( "install", DEFAULT_MVN_PARAMS, setupTest.toString() );
            }
        }
    }

    @Test
    public void testIntegration()
        throws Exception
    {
        String testRelativeLocation = this.testRelativeLocation;
        if ( TestUtils.LOCATION_REWRITE.containsKey( this.testRelativeLocation ) )
        {
            testRelativeLocation = TestUtils.LOCATION_REWRITE.get( this.testRelativeLocation );
        }
        LOGGER.info ("Testing {}", testRelativeLocation);
        String test = getDefaultTestLocation( testRelativeLocation );
        runLikeInvoker( test, null );
    }


    public static boolean setupExists (File test)
    {
        boolean result = false;
        File t1 = new File (DEFAULT_MVN_PARAMS.get( "maven.repo.local" ),"org/commonjava/maven/ext/");
        if ( t1.exists())
        {
            File t2 = new File( t1, test.getName() );
            File[] directories = t2.listFiles(new FileFilter()
            {
                @Override
                public boolean accept( File file )
                {
                    return file.isDirectory();
                }
            });
            if ( directories != null)
            {
                for ( File dir : directories )
                {
                    if ( t2.exists() && dir.exists() && dir.listFiles() != null && dir.listFiles().length > 0)
                    {
                        LOGGER.info( "Setup has already been run for {}", test);
                        return true;
                    }
                }
            }
        }
        return result;
    }
}
