package de.saumya.mojo.rubygems;

import java.io.File;
import java.io.IOException;

import org.jruby.embed.ScriptingContainer;
import org.sonatype.nexus.ruby.DefaultRubygemsGateway;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.cuba.DefaultRubygemsFileSystem;
import org.sonatype.nexus.ruby.cuba.RubygemsFileSystem;
import org.sonatype.nexus.ruby.layout.CachingProxyStorage;
import org.sonatype.nexus.ruby.layout.GETLayout;
import org.sonatype.nexus.ruby.layout.Layout;
import org.sonatype.nexus.ruby.layout.ProxiedRubygemsFileSystem;
import org.sonatype.nexus.ruby.layout.ProxyStorage;
import org.sonatype.nexus.ruby.layout.SimpleStorage;
import org.sonatype.nexus.ruby.layout.Storage;

public class RubygemsServletContextListener extends AbstractRubygemsServletContextListener 
{
    
    public void doContextInitialized( Helper configor ) throws IOException
    {
        // TODO use IsolatedScriptingContainer
        RubygemsGateway gateway = new DefaultRubygemsGateway(new ScriptingContainer());
        File file = configor.getFile( "GEM_PROXY_STORAGE" );
        if ( file != null )
        {
            ProxyStorage storage = new CachingProxyStorage( file, configor.getURL( "GEM_PROXY_URL" ) );
            configor.addStorageAndRegister( "proxy", storage, new NonCachingProxiedRubygemsFileSystem( gateway, storage ) );
        }
        
        file = configor.getFile( "GEM_CACHING_PROXY_STORAGE" );
        if ( file != null )
        {
            ProxyStorage storage = new CachingProxyStorage( file, configor.getURL( "GEM_CACHING_PROXY_URL" ) );
            configor.addStorageAndRegister( "caching", storage, new ProxiedRubygemsFileSystem( gateway, storage ) );
        }
        
        file = new File( configor.getConfigValue( "GEM_HOSTED_STORAGE" ) );
        if ( file != null )
        {
            Storage storage =  new SimpleStorage( file );
            configor.addStorageAndRegister( "hosted", storage, new HostedRubygemsFileSystem( gateway, storage ) );
        }
        
        if ( "true".equals( configor.getConfigValue( "GEM_MERGED" ) ) )
        {
            Storage storage = new MergedSimpleStorage( gateway, configor.storages );
            Layout layout = new GETLayout( gateway, storage );
            RubygemsFileSystem rubygems = new DefaultRubygemsFileSystem( layout,// get requests 
                                                                         null,// no post requests
                                                                         null );// no delete requests
            String key = "merged";
            configor.register( key, RubygemsFileSystem.class, rubygems );
            configor.register( key, Storage.class, storage );
        }
    }

}
